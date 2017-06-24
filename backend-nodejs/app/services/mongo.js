'use strict'

/**
* For example: find property from documents collection
* <pre><code class="javascript">
* var mongo = require('./mongodb.js');
* mongo.find('documents', {type:'flat'}, function(err, result){
*       console.log(result)
* });
* </code></pre>

* @see: https://docs.mongodb.com/manual/crud/
* @see: http://mongodb.github.io/node-mongodb-native/
* @see: https://www.npmjs.com/package/mongodb
*/

/**
 * Constructor
 * @param config Configuration in JSON format.
 * For example: 
 *   "mongodb": {
 *        "url":"mongodb://localhost:27017/test"
 *   },
 */
function GMMongoDB(config) {
    this.config = config;
    this.client = require('mongodb').MongoClient;
    this.ObjectID = require('mongodb').ObjectID;
}

GMMongoDB.prototype = {

    /**
     * Open connection to database
     *
     * @param callback: function(err, db)
     */
    connect: function(callback) {
        this.client.connect(this.config.url, function(err, db) {
            if (err) {
                return callback(err);
            }
            callback(null, db);
        });
    },

    /*
     * Find by query.
     *
     * Query samples:
     * {}: find all documents
     * {_id: ObjectId("575062c147723eee0c422e05")}: find document by id
     * {name: 'Happy House'}: find documents whose name are 'Happy House'
     * {"rooms.type": "bedroom", "rooms.size":{$gt: 60}}: find documents whose area are greater than 60m2
     * 
     * @see: https://docs.mongodb.com/manual/tutorial/query-documents/
     *
     * @param: name - string: collection's name
     * @param query - json object: query string to find.Ex: {name: 'Happy House'} 
     * @param projection - json object: excluded field. Ex: {password: 0}  // 0 -> excluded
     * @param callbacl(err, array of documents) function.
     */
    find: function(name, query, projection, callback) {
        projection = projection || {};
        this.connect(function(err, db) {
            if (err) {
                return callback(err, []);
            }

            try {
                db.collection(name).find(query, projection).toArray(function(err, docs) {
                    db.close();
                    callback(err, docs);
                });

            } catch (ex) {
                db.close();
                callback(ex, []);
            }
        });
    },

    /*
     * Insert a new document to specific collection
     *
     * Sample data:
     * {}: insert a new document without any information. 
     * {type: 'flat', size: 150, rooms:[{type: 'bedroom'}{type:'kitchen'}]}: insert a new property with information.
     * Note: In callback function, call "result.insertedId" to get _id of new property.
     *
     * @param: name - string: collection's name
     * @param data - json object: property information
     * @param callback: function(err, result) function. Can be null
     * result: see http://mongodb.github.io/node-mongodb-native/2.1/api/Collection.html#~insertWriteOpCallback
     * 
     * @see https://docs.mongodb.com/manual/tutorial/insert-documents/
     */
    insert: function(name, data, callback) {
        this.connect(function(err, db) {
            if (err) {
                callback && callback(err, null);
                return;
            }

            try {
                db.collection(name).insertOne(data, function(err, result) {
                    db.close();
                    callback && callback(err, result);
                });

            } catch (ex) {
                db.close();
                callback && callback(ex, null);
            }
        });
    },

    /*
     * Update document
     * This function update only one property at a time.
     *
     * Sample house collection
     * {
     *   _id: "xxx",
     *   size: 100,
     *   rooms:[{
     *       _id: ObjectId("1a7b"),
     *       type: "sitting room",
     *       doors: [{
     *           _id: 1,
     *           name: 'kitchen',
     *           color: 'green'
     *       },{
     *           _id:2,
     *           name: 'bathroom',
     *           color: 'red'
     *       }] 
     *   }]
     * } 
     * 
     * # Change size of house
     * update('houses', {_id: "xxx"}, {$set: {size: '50'}}, callback):
     * 
     * # Change room type. Nested documents in two level
     * update('houses', {_id: "xxx", "rooms._id": db.toObjectId("1a7b")}, {$set: {"rooms.$.type": "kitchen"}}):
     *
     * # Change the whole content of room.
     * update('houses', {_id: "xxx", "rooms._id": roomID}, {$set: {"rooms.$": updateRoom}}):
     *
     * # Add new room to array of room
     * update('houses', {_id: "xxx"}, {$addToSet: {rooms: newRoom}}):
     *
     * # Delete a room to array of room
     * update('houses', {_id: "xxx"}, {$pull: {rooms: {_id: roomId}}}): 
     *
     * # Add new door to rooms. Nested documents in three level
     * update('houses', {_id: "xxx", "rooms._id": roomId}, {$addToSet: {rooms.$.doors: newDoor}}): 
     *
     * # Delete door
     * update('houses', {_id: "xxx", "rooms._id": roomId}, {$pull: {rooms.$.doors: {_id: doorId}}}}): 
     *
     * Note: 
     * When udpate an array of embedded document, we should find exactly element of array to update.
     * And we should not update in more than two level of nested document
     * @See update operator for more detail
     *
     * @param: name - string: collection's name
     * @param query - json: to find where to update. For example: {type: "flat"} -> find property whose type is flat.
     * @param update - json: updating value. For example: {$set: {name: "green garden"}} -> change value of name field to "green garden"
     * @param callback: function(err, result) function. Can be null
     * result: see http://mongodb.github.io/node-mongodb-native/2.1/api/Collection.html#~updateWriteOpCallback
     * @param options: see http://mongodb.github.io/node-mongodb-native/2.1/api/Collection.html#updateOne
     * 
     * @see https://docs.mongodb.com/manual/tutorial/update-documents/
     * @see https://docs.mongodb.com/manual/reference/operator/update/
     */
    update: function(name, query, update, callback, options) {
        this.connect(function(err, db) {
            if (err) {
                callback && callback(err, null);
                return;
            }

            try {
                db.collection(name).updateOne(query, update, options, function(err, result) {
                    db.close();
                    callback && callback(err, result);
                });

            } catch (ex) {
                db.close();
                callback && callback(ex, null);
            }
        });
    },

    /*
     * Delete a document
     * 
     * @param: name - string: collection's name
     * @param query: to find where to delete
     * @param callback: function(err, result). Can be null
     * result: see http://mongodb.github.io/node-mongodb-native/2.1/api/Collection.html#~deleteWriteOpCallback
     */
    delete: function(name, query, callback) {
        this.connect(function(err, db) {
            if (err) {
                callback && callback(err, null);
                return;
            }

            try {
                db.collection(name).deleteOne(query, function(err, result) {
                    db.close();
                    callback && callback(err, result);
                });

            } catch (ex) {
                db.close();
                callback && callback(err, null);
            }
        });
    },

    /**
     * Parse hex string to ObjectID
     * 
     * @param id - string: hex string which contain 24 characters
     * @return mongodb ObjectId.
     * throw an error if id is not 12 bytes or a string of 24 hex characters
     */
    toObjectId: function(id) {
        return this.ObjectID.createFromHexString(id);
    },

    newObjectId: function() {
        var time = Math.floor(new Date().getTime() / 1000);
        return new this.ObjectID(time);
    },

    /**
     * Get auto increment id
     * @param name: value represented for collection which want a auto ID
     * @param callback: function(err, autoID)
     */
    getAutoId: function(name, callback) {
        this.connect(function(err, db) {
            if (err) {
                return callback(err, null);
            }

            try {
                db.collection('app.counters').findAndModify({ _id: name }, [], { $inc: { seq: 1 } }, { new: true, upsert: true }, function(err, doc) {
                    db.close();
                    callback(err, doc.value.seq);
                });

            } catch (ex) {
                db.close();
                callback(err, null);
            }
        });
    },

    /**
     * Drop collection
     * @param name: collection name
     * @param callback: function(err, reply). Can be null
     */
    dropCollection: function(name, callback) {
        this.connect(function(err, db) {
            if (err) {
                callback && callback(err);
                return;
            }

            try {
                var collection = db.collection(name);
                collection.find({}, {}).toArray(function(err, docs) {
                    if (err || docs.length <= 0) {
                        db.close();
                        callback && callback(err);
                        return;
                    }

                    collection.drop(function(err, reply) {
                        db.close();
                        callback(err, reply);
                    });
                });

            } catch (ex) {
                db.close();
                callback && callback(err);
            }
        });
    },

    /**
     * Drop database
     *
     * @param callback(err, result) function. Can be null
     * Result: see http://mongodb.github.io/node-mongodb-native/2.2/api/Db.html#~resultCallback
     */
    dropDatabase: function(callback) {
        this.connect(function(err, db) {
            if (err) {
                callback && callback(err);
                return;
            }

            db.dropDatabase(function(err, result) {
                db.close();
                callback && callback(err, result);
            })
        })
    }
}

// export this class.
module.exports = GMMongoDB;
