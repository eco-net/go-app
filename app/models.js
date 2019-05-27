function w2dcModel(settings) {

    this.settings = settings;
    this.data = "";

}

w2dcModel.prototype.get = function(limit = 5) {


    $.get(this.settings.api + "?per_page=" + limit, function(data, status){
        if(status == 'success') {
            result = JSON.stringify(data);
            this.data = JSON.parse(result);
        } else {
            this.data = false;
        }
    });

return this.data;


};

