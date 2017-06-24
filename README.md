# image-captions-acquisition
The main purposes of this project is to construct a ground-truth dataset in Vietnamese and English for Image Captioning project

# Installation

### Setup Backend
<pre>
$ cd backend-nodejs   
$ npm install    
$ node setup-environment.js --init   
$ node app.js     // start server http:localhost:1508     
</pre>

You can change configurations in configs/setting.json.    
In setting.json, change *mode* to *production* so that log will be stored in logs directory otherwise it will be printed to console    
      
Other setups and commands
<pre>
// reset project
$ node setup-environment.js --reset

// migrate project
$ node migration/migration-controller.js    

// there are some useful scripts, you can checkout in scripts.js file
// Eg, execute a script
$ node scripts.js --version

</pre>

### Setup Android Application
Change *HOST_NAME* in file GMGlobal class to host where you deploy backend nodejs  
Build and run application



## Setup Web Application
Change *base_url* in file GMGlobal class to host where you deploy backend nodejs  
<pre>
$ node build.js
$ You can host app using Apache server.
</pre>


