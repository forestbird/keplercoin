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

import kpl.Asset;
import kpl.db.DbIterator;
import kpl.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class SearchAssets extends APIServlet.APIRequestHandler {

    static final SearchAssets instance = new SearchAssets();

    private SearchAssets() {
        super(new APITag[] {APITag.AE, APITag.SEARCH}, "query", "firstIndex", "lastIndex", "includeCounts");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        String query = Convert.nullToEmpty(req.getParameter("query"));
        if (query.isEmpty()) {
            return JSONResponses.missing("query");
        }
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean includeCounts = "true".equalsIgnoreCase(req.getParameter("includeCounts"));

        JSONObject response = new JSONObject();
        JSONArray assetsJSONArray = new JSONArray();
        try (DbIterator<Asset> assets = Asset.searchAssets(query, firstIndex, lastIndex)) {
            while (assets.hasNext()) {
                assetsJSONArray.add(JSONData.asset(assets.next(), includeCounts));
            }
        }
        response.put("assets", assetsJSONArray);
        return response;
    }

}
