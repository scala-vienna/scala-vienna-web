/* Countdown */

$(function(){
	launchTime = new Date("December 17, 2013 19:02");
	launchTime.setDate(launchTime.getDate());
	$("#countdown").countdown( {
        until: launchTime,
        format: "dHMS",
        onExpiry: function() {
            // we have lift-off!
            document.location.href = "/?lift-off";
        }
    });

});

