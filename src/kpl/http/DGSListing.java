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

package kpl.http;

import kpl.Account;
import kpl.Appendix;
import kpl.Attachment;
import kpl.Constants;
import kpl.kplException;
import kpl.util.Convert;
import kpl.util.JSON;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static kpl.http.JSONResponses.INCORRECT_DGS_LISTING_DESCRIPTION;
import static kpl.http.JSONResponses.INCORRECT_DGS_LISTING_NAME;
import static kpl.http.JSONResponses.INCORRECT_DGS_LISTING_TAGS;
import static kpl.http.JSONResponses.MISSING_NAME;

public final class DGSListing extends CreateTransaction {

    static final DGSListing instance = new DGSListing();

    private DGSListing() {
        super("messageFile", new APITag[] {APITag.DGS, APITag.CREATE_TRANSACTION},
                "name", "description", "tags", "quantity", "priceNQT");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws kplException {

        String name = Convert.emptyToNull(req.getParameter("name"));
        String description = Convert.nullToEmpty(req.getParameter("description"));
        String tags = Convert.nullToEmpty(req.getParameter("tags"));
        long priceNQT = ParameterParser.getPriceNQT(req);
        int quantity = ParameterParser.getGoodsQuantity(req);

        if (name == null) {
            return MISSING_NAME;
        }
        name = name.trim();
        if (name.length() > Constants.MAX_DGS_LISTING_NAME_LENGTH) {
            return INCORRECT_DGS_LISTING_NAME;
        }

        if (description.length() > Constants.MAX_DGS_LISTING_DESCRIPTION_LENGTH) {
            return INCORRECT_DGS_LISTING_DESCRIPTION;
        }

        if (tags.length() > Constants.MAX_DGS_LISTING_TAGS_LENGTH) {
            return INCORRECT_DGS_LISTING_TAGS;
        }

        Appendix.PrunablePlainMessage prunablePlainMessage = (Appendix.PrunablePlainMessage)ParameterParser.getPlainMessage(req, true);
        if (prunablePlainMessage != null) {
            if (prunablePlainMessage.isText()) {
                return MESSAGE_NOT_BINARY;
            }
            byte[] image = prunablePlainMessage.getMessage();
            Tika tika = new Tika();
            String mediaTypeName = tika.detect(image);
            MediaType mediaType = MediaType.parse(mediaTypeName);
            if (mediaType == null || !mediaType.getType().equals("image")) {
                return MESSAGE_NOT_IMAGE;
            }
        }

        Account account = ParameterParser.getSenderAccount(req);
        Attachment attachment = new Attachment.DigitalGoodsListing(name, description, tags, quantity, priceNQT);
        return createTransaction(req, account, attachment);

    }

    private static final JSONStreamAware MESSAGE_NOT_BINARY;
    static {
        JSONObject response = new JSONObject();
        response.put("errorCode", 8);
        response.put("errorDescription", "Only binary message attachments accepted as DGS listing images");
        MESSAGE_NOT_BINARY = JSON.prepare(response);
    }

    private static final JSONStreamAware MESSAGE_NOT_IMAGE;
    static {
        JSONObject response = new JSONObject();
        response.put("errorCode", 9);
        response.put("errorDescription", "Message attachment is not an image");
        MESSAGE_NOT_IMAGE = JSON.prepare(response);
    }

}
