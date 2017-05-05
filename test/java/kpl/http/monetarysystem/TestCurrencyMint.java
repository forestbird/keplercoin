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

package kpl.http.monetarysystem;

import kpl.BlockchainTest;
import kpl.Constants;
import kpl.CurrencyMinting;
import kpl.CurrencyType;
import kpl.crypto.HashFunction;
import kpl.http.APICall;
import kpl.util.Convert;
import kpl.util.Logger;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestCurrencyMint extends BlockchainTest {

    @Test
    public void mint() {
        APICall apiCall = new TestCurrencyIssuance.Builder().
                type(CurrencyType.MINTABLE.getCode() | CurrencyType.EXCHANGEABLE.getCode()).
                maxSupply((long)10000000).
                initialSupply((long)0).
                issuanceHeight(0).
                minting((byte)2, (byte)8, HashFunction.SHA256.getId()).
                build();

        String currencyId = TestCurrencyIssuance.issueCurrencyApi(apiCall);
        mintCurrency(currencyId);
    }

    public void mintCurrency(String currencyId) {
        // Failed attempt to mint
        APICall apiCall = new APICall.Builder("currencyMint").
                secretPhrase(ALICE.getSecretPhrase()).
                feeNQT(Constants.ONE_kpl).
                param("currency", currencyId).
                param("nonce", 123456).
                param("units", 1000).
                param("counter", 1).
                build();
        JSONObject mintResponse = apiCall.invoke();
        Logger.logDebugMessage("mintResponse: " + mintResponse);
        generateBlock();
        apiCall = new APICall.Builder("getCurrency").
                feeNQT(Constants.ONE_kpl).
                param("currency", currencyId).
                build();
        JSONObject getCurrencyResponse = apiCall.invoke();
        Logger.logDebugMessage("getCurrencyResponse: " + getCurrencyResponse);
        Assert.assertEquals("0", getCurrencyResponse.get("currentSupply"));

        // Successful attempt
        long units = 10;
        long algorithm = (Long)getCurrencyResponse.get("algorithm");
        long nonce;
        for (nonce=0; nonce < Long.MAX_VALUE; nonce++) {
            if (CurrencyMinting.meetsTarget(CurrencyMinting.getHash((byte) algorithm, nonce, Convert.parseUnsignedLong(currencyId), units, 1, ALICE.getId()),
                    CurrencyMinting.getTarget(2, 8, units, 0, 100000))) {
                break;
            }
        }
        Logger.logDebugMessage("nonce: " + nonce);
        apiCall = new APICall.Builder("currencyMint").
                secretPhrase(ALICE.getSecretPhrase()).
                feeNQT(Constants.ONE_kpl).
                param("currency", currencyId).
                param("nonce", nonce).
                param("units", units).
                param("counter", 1).
                build();
        mintResponse = apiCall.invoke();
        Logger.logDebugMessage("mintResponse: " + mintResponse);
        generateBlock();
        apiCall = new APICall.Builder("getCurrency").
                feeNQT(Constants.ONE_kpl).
                param("currency", currencyId).
                build();
        getCurrencyResponse = apiCall.invoke();
        Logger.logDebugMessage("getCurrencyResponse: " + getCurrencyResponse);
        Assert.assertEquals("" + units, getCurrencyResponse.get("currentSupply"));

        apiCall = new APICall.Builder("getMintingTarget").
                param("currency", currencyId).
                param("account", ALICE.getId()).
                param("units", "1000").
                build();
        JSONObject getMintingTargetResponse = apiCall.invoke();
        Logger.logDebugMessage("getMintingTargetResponse: " + getMintingTargetResponse);
        Assert.assertEquals("4000", getMintingTargetResponse.get("difficulty"));
        Assert.assertEquals("a9f1d24d62105839b4c876be9f1a2fdd24068195438b6ce7fba9f1d24d621000", getMintingTargetResponse.get("targetBytes"));
    }
}
