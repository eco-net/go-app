$(function() {

App.controller('home', function (page) {
    /*
     $(page)
    .find('.app-button')
    .on('click', function () {
      console.log('button was clicked!');
    });
    */
});

App.controller('dgs', function (page) {

    var data = w2dc.get();

    var $template = $(page).find('.page').remove();
    var $pages = $(page).find('.pages');

    data.forEach(function (data) {

        var $page = $template.clone(true);

        $page.find('.title').text(data.title);
        $page.find('.content' ).text(data.content);

        $pages.append($page);
  });


});


App.controller('contact', function (page, contacts) {
  var $template = $(page).find('.contact').remove();
  var $contacts = $(page).find('.contacts');
  contacts.forEach(function (contact) {
    var $contact = $template.clone(true);
    $contact.find('.first-name').text(contact.firstName);
    $contact.find('.last-name' ).text(contact.lastName );
    $contacts.append($contact);
  });
});


});
