document.addEventListener('DOMContentLoaded', function() {
    var timeLeft = 10;
    var elem = document.getElementById('countdown');
    var container = document.querySelector('main');
    var targetUrl = container ? container.dataset.targetUrl : '/';

    var timerId = setInterval(function() {
        if (timeLeft == 0) {
            clearTimeout(timerId);
            window.location.href = targetUrl;
        } else {
            elem.innerHTML = timeLeft;
            timeLeft--;
        }
    }, 1000);
});
