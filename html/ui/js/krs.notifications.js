/******************************************************************************
 * Copyright © 2013-2016 The KPL Core Developers.                             *
 *                                                                            *
 * See the AUTHORS.txt, DEVELOPER-AGREEMENT.txt and LICENSE.txt files at      *
 * the top-level directory of this distribution for the individual copyright  *
 * holder information and the developer policies on copyright and licensing.  *
 *                                                                            *
 * Unless otherwise agreed in a custom licensing agreement, no part of the    *
 * KPL software, including this file, may be copied, modified, propagated,    *
 * or distributed except according to the terms contained in the LICENSE.txt  *
 * file.                                                                      *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************/

/**
 * @depends {krs.js}
 */
var krs = (function(krs, $, undefined) {

	krs.updateNotificationUI = function() {
		var subTypeCount = 0;
		var totalCount = 0;

		var $menuItem = $('#notification_menu');
		var $popoverItem = $("<div id='notification_popover'></div>");
		
		$.each(krs.transactionTypes, function(typeIndex, typeDict) {
			$.each(typeDict["subTypes"], function(subTypeIndex, subTypeDict) {
				if (subTypeDict["notificationCount"] > 0) {
					subTypeCount += 1;
					totalCount += subTypeDict["notificationCount"];
					var html = "";
					html += "<a href='#' style='display:block;background-color:#f0f0f0;border:1px solid #e2e2e2;padding:4px 12px 9px 12px;margin:2px;'>";
					html += "<div style='float:right;'><div style='display:inline-block;margin-top:2px;'>";
					html += "<span class='badge' style='background-color:#e65;'>" + subTypeDict["notificationCount"] + "</span>";
					html += "</div></div>";
					html += krs.getTransactionIconHTML(typeIndex, subTypeIndex) + "&nbsp; ";
					html += '<span style="font-size:12px;color:#000;display:inline-block;margin-top:5px;">';
					html += $.t(subTypeDict['i18nKeyTitle'], subTypeDict['title']);
					html += '</span>';
					html += "</a>";

					var $subTypeItem = $(html);
					$subTypeItem.click(function(e) {
						e.preventDefault();
						krs.goToPage(subTypeDict["receiverPage"]);
						$menuItem.popover('hide');
					});
					$subTypeItem.appendTo($popoverItem);
				}
			});
		});
		if (totalCount > 0) {
			$menuItem.find('span .nm_inner_subtype').css('backgroundColor', '#337ab7');
			$menuItem.find('span .nm_inner_total').css('backgroundColor', '#e06054');

			var $markReadDiv = $("<div style='text-align:center;padding:12px 12px 8px 12px;'></div>");
			var $markReadLink= $("<a href='#' style='color:#d1a400;'>" + $.t('notifications_mark_as_read', 'Mark all as read') + "</a>");
			$markReadLink.click(function(e) {
				e.preventDefault();
				krs.resetNotificationState();
				$menuItem.popover('hide');
			});
			$markReadLink.appendTo($markReadDiv);
			$popoverItem.append($markReadDiv);
			document.title = $.t('app_title') + ' (' + String(totalCount) + ')';
		} else {
			$menuItem.find('span .nm_inner_subtype').css('backgroundColor', '#d1a400');
			$menuItem.find('span .nm_inner_total').css('backgroundColor', '');
			var html = "";
			html += "<div style='text-align:center;padding:12px;'>" + $.t('no_notifications', 'No current notifications') + "</div>";
			$popoverItem.append(html);
			document.title = $.t('app_title');
		}

		$menuItem.find('span .nm_inner_subtype').html(String(subTypeCount));
		$menuItem.find('span .nm_inner_total').html(String(totalCount));
		$menuItem.show();

		var template = '<div class="popover" style="min-width:320px;"><div class="arrow"></div><div class="popover-inner">';
		template += '<h3 class="popover-title"></h3><div class="popover-content"><p></p></div></div></div>';

		if($menuItem.data('bs.popover')) {
    		$menuItem.data('bs.popover').options.content = $popoverItem;
		} else {
			$menuItem.popover({
				"html": true,
				"content": $popoverItem,
				"trigger": "click",
				template: template
			});
		}
	};

	krs.saveNotificationTimestamps = function() {
		var tsDict = {};
		$.each(krs.transactionTypes, function(typeIndex, typeDict) {
			$.each(typeDict["subTypes"], function(subTypeIndex, subTypeDict) {
				var tsKey = "ts_" + String(typeIndex) + "_" + String(subTypeIndex);
				tsDict[tsKey] = subTypeDict["notificationTS"];
			});
		});
		var tsDictString = JSON.stringify(tsDict);
		krs.storageSelect("data", [{
			"id": "notification_timestamps"
		}], function(error, result) {
			if (result && result.length > 0) {
				krs.storageUpdate("data", {
					contents: tsDictString
				}, [{
					id: "notification_timestamps"
				}]);
			} else {
				krs.storageInsert("data", "id", {
					id: "notification_timestamps",
					contents: tsDictString
				});
			}
		});
	};

	krs.resetNotificationState = function(page) {
		krs.sendRequest("getTime", {}, function(response) {
			if (response.time) {
				$.each(krs.transactionTypes, function(typeIndex, typeDict) {
					$.each(typeDict["subTypes"], function(subTypeIndex, subTypeDict) {
						if (!page || (page && subTypeDict["receiverPage"] == page)) {
							var countBefore = subTypeDict["notificationCount"];
							if (subTypeDict["lastKnownTransaction"]) {
								subTypeDict["notificationTS"] = subTypeDict["lastKnownTransaction"].timestamp + 1;
							} else {
								subTypeDict["notificationTS"] = response.time;
							}
							subTypeDict["notificationCount"] = 0;
							typeDict["notificationCount"] -= countBefore;
						}
					});
				});
				krs.saveNotificationTimestamps();
				krs.updateNotificationUI();
			}
		});
	};

	krs.initNotificationCounts = function(time) {
		var fromTS = time - 60 * 60 * 24 * 14;
		krs.sendRequest("getBlockchainTransactions+", {
			"account": krs.account,
			"timestamp": fromTS,
			"firstIndex": 0,
			"lastIndex": 99
		}, function(response) {
			if (response.transactions && response.transactions.length) {
				for (var i=0; i<response.transactions.length; i++) {
					var t = response.transactions[i];
					var subTypeDict = krs.transactionTypes[t.type]["subTypes"][t.subtype];
					if (t.recipient && t.recipient == krs.account && subTypeDict["receiverPage"]) {
						if (!subTypeDict["lastKnownTransaction"] || subTypeDict["lastKnownTransaction"].timestamp < t.timestamp) {
							subTypeDict["lastKnownTransaction"] = t;
						}
						if (t.timestamp > subTypeDict["notificationTS"]) {
							krs.transactionTypes[t.type]["notificationCount"] += 1;
							subTypeDict["notificationCount"] += 1;
						}	
					}
				}
			}
			krs.updateNotificationUI();
		});
	};

	krs.loadNotificationsFromTimestamps = function(time, tsDictString) {
		var tsDict = {};
		if (tsDictString != "") {
			tsDict = JSON.parse(tsDictString);
		}

		$.each(krs.transactionTypes, function(typeIndex, typeDict) {
			typeDict["notificationCount"] = 0;
			$.each(typeDict["subTypes"], function(subTypeIndex, subTypeDict) {
				var tsKey = "ts_" + String(typeIndex) + "_" + String(subTypeIndex);
				if (tsDict[tsKey]) {
					subTypeDict["notificationTS"] = tsDict[tsKey];
				} else {
					subTypeDict["notificationTS"] = time;
				}
				subTypeDict["notificationCount"] = 0;
			});
		});
		krs.initNotificationCounts(time);
		krs.saveNotificationTimestamps();
	};

	krs.updateNotifications = function() {
		krs.sendRequest("getTime", {}, function(response) {
			if (response.time) {
				var tsDictString = "";
				krs.storageSelect("data", [{
					"id": "notification_timestamps"
				}], function(error, result) {
					//console.log(result);
					if (result) {
						if (result.length > 0) {
							tsDictString = result[0].contents;
							krs.loadNotificationsFromTimestamps(response.time, tsDictString);
						} else {
							krs.loadNotificationsFromTimestamps(response.time, "");
						}
					}
				});
			}
		});
	};

	krs.setUnconfirmedNotifications = function() {
		$('#unconfirmed_notification_counter').html(String(krs.unconfirmedTransactions.length));
		$('#unconfirmed_notification_menu').show();
	};

	krs.setPhasingNotifications = function() {
		krs.sendRequest("getAccountPhasedTransactionCount", {
			"account": krs.account
		}, function(response) {
			//noinspection JSUnresolvedVariable
			if (response.numberOfPhasedTransactions != undefined) {
				//noinspection JSUnresolvedVariable
				$('#phasing_notification_counter').html(String(response.numberOfPhasedTransactions));
				$('#phasing_notification_menu').show();
			}
		});
	};


	return krs;
}(krs || {}, jQuery));
