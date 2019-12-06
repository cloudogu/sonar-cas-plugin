function logoutMenuHandler() {
    var menuHandlerTimer = setInterval(function () {
        var elem1 = document.getElementById('global-navigation');
        if (!elem1) {
            return;
        }
        var elem2 = elem1.getElementsByClassName('js-user-authenticated')[0];
        if (!elem2) {
            return;
        }
        var elem3 = elem2.getElementsByTagName('a')[0];
        if (elem3) {
            elem3.addEventListener('click', logoutHandler);

            clearInterval(menuHandlerTimer);
        }
    }, 250);
}

function logoutHandler() {
    var logoutHandlerTimer = setInterval(function () {
        var elem1 = document.getElementById('global-navigation');
        if (! elem1) { return; }
        var elem2 = elem1.getElementsByClassName('js-user-authenticated')[0];
        if (! elem2) { return; }
        var elem3 = elem2.getElementsByClassName('popup')[0];
        if (! elem3) { return; }
        var elem4 = elem3.getElementsByTagName('a')[1];
        if (elem4) {
            elem4.addEventListener('click', function (event) {
                window.location.href = 'CASLOGOUTURL';
                event.stopImmediatePropagation();
                return false;
            });
            clearInterval(logoutHandlerTimer);
        }

    }, 100);
}
