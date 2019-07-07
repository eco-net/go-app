
function fetch_and_display_posts(limit)
{

       $.get(settings.api + "?per_page=" + limit, function(data, status){
        if(status == 'success') {

           var posts = JSON.parse(JSON.stringify(data));

            var html = "";

            for(var count = 0; count < posts.length; count++)
            {
                var title = posts[count]['title']['rendered'];
                var link = posts[count]['link'];
                var date = posts[count]['date'];
                var image = posts[count]['images']['medium'];

                html = html + "<li>" + "<a href='javascript:open_browser(\"" + link + "\")'>" + "<img height='128' width='128' src='" + image + "'>" + "<h2>" + title + "</h2>" + "<p>" + date + "</p></a></li>";
            }

            document.getElementById("posts").innerHTML = html;

            $('#post-page').bind('pageinit', function() {
                $("#posts").listview("refresh");
            });

        } else {
            alert('error');
        }
    });

/*
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "https://grontoverblik.dk/wp-json/wp/v2/go/?per_page=1");
    xhr.onload = function(){
        var posts_array = JSON.parse(xhr.responseText);

        var html = "";

        for(var count = 0; count < posts_array.length; count++)
        {
                    console.log(posts_array[count]);
            var title = posts_array[count]['title']['rendered'];
            var link = posts_array[count]['link'];
            var date = posts_array[count]['date'];
            var image = posts_array[count]['images']['medium'];

            html = html + "<li>" + "<a href='javascript:open_browser(\"" + link + "\")'>" + "<img height='128' width='128' src='" + image + "'>" + "<h2>" + title + "</h2>" + "<p>" + date + "</p></a></li>";
        }

        document.getElementById("posts").innerHTML = html;
        $('#post-page').bind('pageinit', function() {
            $("#posts").listview("refresh");
        });
    }
    xhr.send();
    */
}

function login()
{
    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;

    if(username == "")
    {
        navigator.notification.alert("Please enter username", null, "Username Missing", "OK");
        return;
    }

    if(password == "")
    {
        navigator.notification.alert("Please enter password", null, "Password Missing", "OK");  
        return;
    }

    var xhr = new XMLHttpRequest();
    xhr.open("GET", "http://localhost/wp-admin/admin-ajax.php?action=login&username=" + encodeURIComponent(username) + "&password=" + encodeURIComponent(password));
    xhr.onload = function(){
        if(xhr.responseText == "FALSE")
        {
            navigator.notification.alert("Wrong Username and Password", null, "Wrong Creds", "Try Again");
        }
        else if(xhr.responseText == "TRUE")
        {
            fetch_and_display_posts();
            $("#page_two_link").click();
        }
    }   
    xhr.send();
}

function open_browser(link)
{
    window.open(link, '_blank', 'location=yes');
}
