function sortItemByTimeFunction(a, b) {
    if (a.getStartTime() > b.getStartTime()) return 1;

    if (a.getStartTime() < b.getStartTime()) return -1;

    return 0;
}

/**
 * Open info window for markers (not polygons or polylines)
 */
TimeMapItem.openInfoWindowMap = function () {
    var item = this,
        html = item.getInfoHtml(),
        ds = item.dataset,
        placemark = item.placemark,
        datasets = ds.timemap.datasets;

    if (item.getType() == "marker" && placemark.api) {
        // scroll timeline if necessary
        if (!item.onVisibleTimeline()) {
            ds.timemap.scrollToDate(item.getStart());
        }

        var allItemsHere = new Array();
	allItemsHere.push(item);

        var thisLocation = item.placemark.location;
        var thisLat = thisLocation.lat;
        var thisLon = thisLocation.lon;
        var thisTitle = item.opts.title;
        var thisStartTime = item.getStartTime();

        var dsLength = ds.items.length;

        for (var i = 0; i < dsLength; i++) {

	var theItem = ds.items[i];
                 if (theItem.getType() === "marker") {
   	 var placemarkVisible = theItem.placemarkVisible;
            var theLocation = theItem.placemark.location;
            var theLat = theLocation.lat;
            var theLon = theLocation.lon;
            var theTitle = theItem.opts.title;
            var theStartTime = theItem.getStartTime();

            if (theLat == thisLat && theLon == thisLon) {

                    if (placemarkVisible) {
                        if (!(thisTitle == theTitle &&
                            thisStartTime == theStartTime)){
                            allItemsHere.push(theItem);
                        }
                        }
                    }
                }
            }

            if (allItemsHere.length > 1) {
		html = "";
                allItemsHere.sort(sortItemByTimeFunction);

                var i = 0;
                for (i = 0; i < allItemsHere.length; i++) {
                    var currentItem = allItemsHere[i];
			var currentItemHtml =  currentItem.getInfoHtml();
                    html = html + currentItemHtml;
                }
            }

            placemark.setInfoBubble(html);
            placemark.openBubble();
            // deselect when window is closed
            item.closeHandler = placemark.closeInfoBubble.addHandler(function () {
                // deselect
                ds.timemap.setSelected(undefined);
                // kill self
                placemark.closeInfoBubble.removeHandler(item.closeHandler);
            });
        }
    }

    /**
     * Don't display window for this item.
     */
    TimeMapItem.openInfoWindowTimeline = function () {
    };
    
TimeMapItem.closeInfoWindowTimeline = function() {
    var item = this;
    if (item.getType() == "marker") {
        //item.placemark.closeBubble();
    } else {
        if (item == item.map.tmBubbleItem) {
            item.map.closeBubble();
           // item.map.tmBubbleItem = null;
        }
    }
};

TimeMapItem.prototype.showEvent = function() {
        var item = this,
            event = item.event;
	
	if(item.dataset.opts.noEventLoad){
		return;
	}
	
        if (event && !item.eventVisible) {
            // XXX: Use timeline filtering API
            item.timeline.getBand(0)
                .getEventSource()._events._events.add(event);
            item.eventVisible = true;
        }
    };
 