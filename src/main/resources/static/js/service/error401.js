$( window ).bind('load',function() {
    var cookie = $.cookie("token");
    console.log(cookie)
    fetch('http://localhost:5557/api/auth/authenticated',{
        method: 'GET',
        headers: new Headers({
            'Content-Type': 'application/json',
            'Authorization': cookie
        })
    })
        .then(response =>  {
            if (response.status === 401 && window.location.href !== "http://localhost:5557/") {
                window.location.href = '/'
            }
        }).catch( error => error.response.then(message => console.log(message)));
})