var tree = require('./tree');

exports.createTree = function (data, containerSelector, emptyText, reverse, settings) {
    new tree.Tree(data, containerSelector, emptyText, settings, reverse);
};

exports.createReverseTree = function (data, containerSelector, emptyText, settings) {
    new tree.Tree(data, containerSelector, emptyText, settings, true);
};
