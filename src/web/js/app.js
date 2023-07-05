var $ = require('jquery-browserify')
var network = require('./modules/network/network');
var ajax = require('./modules/ajax');
var modal = require('./modules/modal');

window.network = network;
window.reverseTreeModal = function (url, title) {
    modal.showModal(title);
    $(".modal-content").html("<div class='network w-full h-[600px]'></div>")
    ajax.get(url, function (response) {
        network.createReverseTree(response, ".network", "No data");
    });
}

window.explainRelationshipModal = function (target, relationship, relatedEntity, intermediateEntity) {
    modal.showModal("Relationship details");
    let data = {
        target: target,
        relationship: relationship,
        relatedEntity: relatedEntity
    }
    if (intermediateEntity) {
        data.intermediateEntity = intermediateEntity
    }
    ajax.post("/relationships/explain", data, function (response) {
        $(".modal-content").html(response);
    });
}
