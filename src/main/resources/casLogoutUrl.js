function logoutHandler() {
    var timer = setInterval(function () {
        var elem1 = document.getElementById('global-navigation');
        if (! elem1) { console.log("could not find 1"); return; }
        var elem2 = elem1.getElementsByClassName('js-user-authenticated')[0];
        if (! elem2) { console.log("could not find 2"); return; }
        var elem3 = elem2.getElementsByClassName('dropdown-menu')[0];
        if (! elem3) { console.log("could not find 3"); return; }
        var elem4 = elem3.getElementsByTagName('a')[1];
        if (elem4) {
            elem4.addEventListener('click', function () {
                window.location.href = 'CASLOGOUTURL';
                return true;
            });
            clearInterval(timer);
        }

    }, 500);
}