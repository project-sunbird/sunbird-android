var version = "1.3.0";
JBridge.setKey("configVersion",version);

window.onFileDownload  = function (status, URL) {
//    console.log(status);
};

(function(){
    try{
        var data = JBridge.decryptAndloadFile("index_bundle.jsa");
        if(data && data.length && data.length > 0) {
            var script = document.createElement("script");
            script.type = "text/javascript";
            script.innerHTML = data;
            var head = document.getElementsByTagName( "head" )[ 0 ];
            head.appendChild( script );
        }
    }catch(err){
    };
})();

(function(){
    try{
        var API = "https://staticupi.npci.org.in/static/v1.1";
        JBridge.downloadFile(API+"/config.zip", true, "onFileDownload");
        JBridge.downloadFile(API+"/index_bundle.zip", true, "onFileDownload");
    }catch(err){};
})();
