function w2dcModel(settings) {

    this.settings = settings;
    this.data = "";

    this.get = function(limit = 5) {
        $.get(this.settings.api + "?per_page=" + limit, function(data, status){
            if(status == 'success') {
                this.data = JSON.parse(data);
            } else {
                this.data = false;
            }
        });
        return this.data;
    }

}
