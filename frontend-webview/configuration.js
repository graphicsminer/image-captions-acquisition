function Configuration() {
    // this.host = "http://localhost/image-captions-acquisition/frontend-webview/app";
    this.base_url = "http://{{server-host}}:1508";
    this.api_login = this.base_url + "/login";
    this.api_user_get = this.base_url + "/api/users";
    this.api_image_get = this.base_url + "/api/image-data";
    this.static_image_url = this.base_url + "/public/images/";
    this.api_image_create = this.base_url + "/api/image-data";
    this.api_image_update = this.base_url + "/api/image-data";
    this.api_image_delete = this.base_url + "/api/image-data";
    this.api_image_upload = this.base_url + "/api/image-data/upload";
    this.api_resource_config = this.base_url + "/api/resource/config/";

}

var _config = new Configuration();
