let networkUtils = require('./network-utils');

class Tree {

    constructor(data, containerSelector, emptyText, settings, reverse = false) {
        this.data = data;
        this.containerSelector = containerSelector;
        this.settings = Object.assign(networkUtils.DEFAULT_NETWORK_SETTINGS, settings);
        this.emptyText = emptyText;
        this.reverse = reverse;
        this.renderTree();
    }

    renderTree() {
        let self = this;

        if (this.data.nodes.length === 0 || this.data.relationships.length === 0) {
            networkUtils.emptyNetwork(this.containerSelector, this.emptyText);
        } else {
            let hierarchy = networkUtils.createHierarchy(self.containerSelector, self.data.rootId, self.data.nodes, self.getRelationships(), self.settings, self.reverse, self.data.extraNode);

            networkUtils.drawLinks(hierarchy.g, hierarchy.root, this.getRelationships());
            let mainNodesG = hierarchy.g.selectAll(".node");

            self.addExtraNode(hierarchy.g, hierarchy.root);

            networkUtils.drawNodes(hierarchy.root, mainNodesG, hierarchy.root, this.settings, this.data.extraNode);
        }
    }

    getRelationships() {
        return this.reverse ? networkUtils.reverseRelationships(this.data.relationships) : this.data.relationships;
    }

    addExtraNode(g, root) {
        let self = this;

        if (self.data.extraNode) {
            let relatedToNode = root.find(function (node) {
                return node.data.id === self.data.extraNode.relatedTo;
            });

            let linkFrom = {x: root.computeX(relatedToNode.x), y: root.computeY(relatedToNode.y)};
            let linkTo = {
                x: root.computeX(relatedToNode.x) + self.settings.extraNodeDistance,
                y: root.computeY(relatedToNode.y)
            };
            let linkDirection = self.data.extraNode.relationshipDirection;
            g.append("path")
                .attr("class", "relationship")
                .attr("d", linkDirection === 'INCOMING' ? `M ${linkFrom.x} ${linkFrom.y} L ${linkTo.x} ${linkTo.y}` : `M ${linkTo.x} ${linkTo.y} L ${linkFrom.x} ${linkFrom.y}`)
                .attr('marker-end', 'url(#arrow)');

            let textPosition = {
                x: linkFrom.x + self.settings.nodeRadius,
                y: linkFrom.y - self.settings.nodeRadius + self.settings.textMargin / 2
            }

            g.append("foreignObject")
                .attr("width", self.settings.extraNodeDistance - self.settings.nodeRadius * 2)
                .attr("height", self.settings.textMargin * 2)
                .attr("transform", `translate(${textPosition.x}, ${textPosition.y})`)
                .html(networkUtils.relationshipText(self.data.extraNode.relationship, true, "extra-node-relationship"));

            let extraNodeG = g.append("g").attr("class", networkUtils.nodeClass(self.data.extraNode.node, false));
            let circleG = extraNodeG.append("g")
                .attr("class", "node-circle")
                .attr("transform", `translate(${linkTo.x},${linkTo.y})`)
            networkUtils.addNodeIcon(circleG, self.settings, _ => self.data.extraNode.node);

            extraNodeG.append("foreignObject")
                .attr("transform", `translate(${linkTo.x + self.settings.nodeRadius},${-self.settings.nodeRadius - self.settings.textMargin})`)
                .attr("width", self.settings.nodeLabelWidth - self.settings.textMargin)
                .attr("height", self.settings.nodeRadius * 2 + self.settings.textMargin * 2)
                .html(networkUtils.labelHtml(self.data.extraNode.node, self.settings));
        }
    }
}

exports.Tree = Tree
