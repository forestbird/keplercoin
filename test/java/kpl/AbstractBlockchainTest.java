/******************************************************************************
 * Copyright © 2013-2016 The kpl Core Developers.                             *
 *                                                                            *
 * See the AUTHORS.txt, DEVELOPER-AGREEMENT.txt and LICENSE.txt files at      *
 * the top-level directory of this distribution for the individual copyright  *
 * holder information and the developer policies on copyright and licensing.  *
 *                                                                            *
 * Unless otherwise agreed in a custom licensing agreement, no part of the    *
 * kpl software, including this file, may be copied, modified, propagated,    *
 * or distributed except according to the terms contained in the LICENSE.txt  *
 * file.                                                                      *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************/

package kpl;

import kpl.crypto.Crypto;
import kpl.util.Listener;
import kpl.util.Logger;
import org.junit.Assert;

import java.util.Properties;

public abstract class AbstractBlockchainTest {

    protected static BlockchainProcessorImpl blockchainProcessor;
    protected static BlockchainImpl blockchain;
    private static final Object doneLock = new Object();
    private static boolean done = false;

    protected static Properties newTestProperties() {
        Properties testProperties = new Properties();
        testProperties.setProperty("kpl.shareMyAddress", "false");
        testProperties.setProperty("kpl.savePeers", "false");
        //testProperties.setProperty("kpl.enableAPIServer", "false");
        //testProperties.setProperty("kpl.enableUIServer", "false");
        testProperties.setProperty("kpl.disableGenerateBlocksThread", "true");
        //testProperties.setProperty("kpl.disableProcessTransactionsThread", "true");
        //testProperties.setProperty("kpl.disableRemoveUnconfirmedTransactionsThread", "true");
        //testProperties.setProperty("kpl.disableRebroadcastTransactionsThread", "true");
        //testProperties.setProperty("kpl.disablePeerUnBlacklistingThread", "true");
        //testProperties.setProperty("kpl.getMorePeers", "false");
        testProperties.setProperty("kpl.testUnconfirmedTransactions", "true");
        testProperties.setProperty("kpl.debugTraceAccounts", "");
        testProperties.setProperty("kpl.debugLogUnconfirmed", "false");
        testProperties.setProperty("kpl.debugTraceQuote", "\"");
        //testProperties.setProperty("kpl.numberOfForkConfirmations", "0");
        return testProperties;
    }

    protected static void init(Properties testProperties) {
        Kpl.init(testProperties);
        blockchain = BlockchainImpl.getInstance();
        blockchainProcessor = BlockchainProcessorImpl.getInstance();
        blockchainProcessor.setGetMoreBlocks(false);
        TransactionProcessorImpl.getInstance().clearUnconfirmedTransactions();
        Listener<Block> countingListener = block -> {
            if (block.getHeight() % 1000 == 0) {
                Logger.logMessage("downloaded block " + block.getHeight());
            }
        };
        blockchainProcessor.addListener(countingListener, BlockchainProcessor.Event.BLOCK_PUSHED);
    }

    protected static void shutdown() {
        TransactionProcessorImpl.getInstance().clearUnconfirmedTransactions();
    }

    protected static void downloadTo(final int endHeight) {
        if (blockchain.getHeight() == endHeight) {
            return;
        }
        Assert.assertTrue(blockchain.getHeight() < endHeight);
        Listener<Block> stopListener = block -> {
            if (blockchain.getHeight() == endHeight) {
                synchronized (doneLock) {
                    done = true;
                    blockchainProcessor.setGetMoreBlocks(false);
                    doneLock.notifyAll();
                    throw new kplException.StopException("Reached height " + endHeight);
                }
            }
        };
        blockchainProcessor.addListener(stopListener, BlockchainProcessor.Event.BLOCK_PUSHED);
        synchronized (doneLock) {
            done = false;
            Logger.logMessage("Starting download from height " + blockchain.getHeight());
            blockchainProcessor.setGetMoreBlocks(true);
            while (! done) {
                try {
                    doneLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        Assert.assertEquals(endHeight, blockchain.getHeight());
        blockchainProcessor.removeListener(stopListener, BlockchainProcessor.Event.BLOCK_PUSHED);
    }

    protected static void forgeTo(final int endHeight, final String secretPhrase) {
        if (blockchain.getHeight() == endHeight) {
            return;
        }
        Assert.assertTrue(blockchain.getHeight() < endHeight);
        Listener<Block> stopListener = block -> {
            if (blockchain.getHeight() == endHeight) {
                synchronized (doneLock) {
                    done = true;
                    Generator.stopForging(secretPhrase);
                    doneLock.notifyAll();
                }
            }
        };
        blockchainProcessor.addListener(stopListener, BlockchainProcessor.Event.BLOCK_PUSHED);
        synchronized (doneLock) {
            done = false;
            Logger.logMessage("Starting forging from height " + blockchain.getHeight());
            Generator.startForging(secretPhrase);
            while (! done) {
                try {
                    doneLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        Assert.assertTrue(blockchain.getHeight() >= endHeight);
        Assert.assertArrayEquals(Crypto.getPublicKey(secretPhrase), blockchain.getLastBlock().getGeneratorPublicKey());
        blockchainProcessor.removeListener(stopListener, BlockchainProcessor.Event.BLOCK_PUSHED);
    }
}
