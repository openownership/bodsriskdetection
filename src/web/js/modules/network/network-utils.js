exports.DEFAULT_NETWORK_SETTINGS = {
    horizontalGap: 300,
    verticalGap: 200,
    nodeRadius: 30,
    padding: 100,
    nodeLabelWidth: 250,
    textMargin: 10,
    extraNodeDistance: 300,
    highlightOnHover: true,

    nodeUrl: function (nodeData) {
        return `/profile/${encodeURIComponent(nodeData.id)}/risks`;
    },

    //TODO: This is a bit hacky, this component shouldn't know about "risks" and "contracts"
    tagUrl: function (nodeData, tag) {
        if (tag.type === "RISKS") {
            return `/profile/${encodeURIComponent(nodeData.id)}/risks`;
        } else {
            return `/profile/${encodeURIComponent(nodeData.id)}/public-contracts`;
        }
    }
};

NETWORK_LINK_TYPES = {
    STRAIGHT: "straight",
    STRAIGHT_AND_TURN: "straight-and-turn",
    TURN_AND_STRAIGHT: "turn-and-straight"
}

function addSvg(containerSelector) {
    function addArrowPointerDef(svg) {
        const markerBoxWidth = 1200;
        const markerBoxHeight = 1200;
        const refX = markerBoxWidth / 2 + 3500;
        const refY = markerBoxHeight / 2;
        const markerWidth = 6;
        const markerHeight = 6;
        const arrowPoints = [[0, 0], [0, 1200], [1200, 600]];

        svg.append('defs')
            .append('marker')
            .attr('id', 'arrow')
            .attr('viewBox', [0, 0, markerBoxWidth, markerBoxHeight])
            .attr('refX', refX)
            .attr('refY', refY)
            .attr('markerWidth', markerWidth)
            .attr('markerHeight', markerHeight)
            .attr('orient', 'auto-start-reverse')
            .append('path')
            .attr('d', d3.line()(arrowPoints))
    }

    let svg = d3.select(containerSelector).append("svg")
        .attr("width", "100%")
        .attr("height", "100%");
    addArrowPointerDef(svg);

    svg.append("rect")
        .attr("id", "network-canvas")
        .attr("width", "100%")
        .attr("height", "100%");

    return svg;
}

function realTreeSize(root, networkSettings, extraNode) {
    let extentX = d3.extent(root, d => d.x);
    let extentY = d3.extent(root, d => d.y);
    let realWidth = extentX[1] - extentX[0] + (extraNode ? networkSettings.extraNodeDistance : 0) + networkSettings.nodeRadius;
    let realHeight = extentY[1] - extentY[0];
    return {
        width: realWidth,
        height: realHeight
    }
}

function centerTree(containerSelector, svg, g, root, networkSettings, extraNode) {
    let zoom = d3.zoom()
        .filter((e) => {
            if (e.type === "wheel") {
                return e.metaKey || e.ctrlKey
            }
            return true;
        })
        .on("zoom", function (e) {
            g.attr('transform', e.transform)
        });

    let realSize = realTreeSize(root, networkSettings, extraNode);
    svg.call(zoom);

    let xRatio = ($(containerSelector).width() - networkSettings.nodeLabelWidth * 2 - networkSettings.padding) / realSize.width;
    let yRatio = ($(containerSelector).height() - networkSettings.nodeRadius * 2 - networkSettings.padding) / realSize.height;
    let ratio = Math.min(xRatio, yRatio);
    let centerX = realSize.width / 2;
    let centerY = realSize.height / 2;

    zoom.scaleTo(svg, ratio);
    zoom.translateTo(svg, centerX, centerY);
    return realSize;
}

function getLinkCoordinates(root, d, networkSettings, upsideDown) {
    let coordinates = {
        fromX: root.computeX(d.parent.x),
        fromY: root.computeY(d.parent.y),
        intermediateX: root.computeX(d.x),
        intermediateY: root.computeY(d.parent.y + networkSettings.verticalGap / 2 - networkSettings.nodeRadius),
        toX: root.computeX(d.x),
        toY: root.computeY(d.y),
        linkType: function () {
            if (this.fromX === this.intermediateX && this.intermediateX === this.toX) {
                return NETWORK_LINK_TYPES.STRAIGHT
            } else if (Math.abs(this.intermediateX - this.fromX) > 0) {
                return NETWORK_LINK_TYPES.TURN_AND_STRAIGHT
            } else {
                return NETWORK_LINK_TYPES.STRAIGHT_AND_TURN
            }
        }
    }

    if (upsideDown) {
        coordinates.fromX = root.computeX(d.x);
        coordinates.fromY = root.computeY(d.y);
        coordinates.intermediateY = root.computeY(d.y - networkSettings.verticalGap / 2);
        coordinates.toX = root.computeX(d.parent.x);
        coordinates.toY = root.computeY(d.parent.y);
    }

    return coordinates;
}

function getRelationshipTextBox(root, d, networkSettings) {
    let defaultWidth = networkSettings.horizontalGap - networkSettings.nodeRadius;
    let fullHeight = networkSettings.verticalGap - networkSettings.nodeRadius * 2;
    let linkCoordinates = d.data.linkCoordinates;
    if (linkCoordinates.linkType() === NETWORK_LINK_TYPES.STRAIGHT) {
        return {
            x: linkCoordinates.fromX,
            y: linkCoordinates.fromY + networkSettings.nodeRadius,
            width: defaultWidth,
            height: fullHeight
        }
    } else if (linkCoordinates.linkType() === NETWORK_LINK_TYPES.TURN_AND_STRAIGHT) {
        return {
            x: linkCoordinates.toX,
            y: linkCoordinates.intermediateY,
            width: defaultWidth,
            height: fullHeight / 2
        }
    } else {
        return {
            x: linkCoordinates.fromX,
            y: linkCoordinates.fromY + networkSettings.nodeRadius,
            width: defaultWidth,
            height: fullHeight / 2
        }
    }
}

exports.createHierarchy = function (containerSelector, rootId, nodes, relationships, networkSettings, upsideDown, extraNode) {
    let mainNode = nodes.find(node => node.id === rootId);
    let hierarchy = d3.hierarchy(mainNode, function (d) {
        return relationships
            .filter(rel => rel.parentId === d.id)
            .map(function (relationship) {
                return nodes.find(node => node.id === relationship.childId)
            });
    });
    let tree = d3.tree()
        .nodeSize([networkSettings.horizontalGap, networkSettings.verticalGap])
        .separation(function separation(a, b) {
            return 1
        });

    let root = tree(hierarchy);

    let svg = addSvg(containerSelector);
    let g = svg.append("g");
    let realSize = centerTree(containerSelector, svg, g, root, networkSettings, extraNode);

    // When using d3.tree().nodeSize the X coordinates can be negative as the root node has x=0
    let shiftX = Math.abs(d3.min(root, d => d.x));

    root.computeX = x => x + shiftX;
    root.computeY = y => upsideDown ? realSize.height - y : y;
    root.upsideDown = upsideDown;

    hierarchy.each(function (d) {
        if (d.data.id !== root.data.id) {
            d.data.linkCoordinates = getLinkCoordinates(root, d, networkSettings, upsideDown);
            d.data.relationshipTextBox = getRelationshipTextBox(root, d, networkSettings);
        }
    });

    return {root: root, svg: svg, g: g};
}


function addNodeIcon(g, networkSettings, getNodeData) {
    g.append("circle")
        .attr("r", networkSettings.nodeRadius)
    g.append("image")
        .attr("x", -networkSettings.nodeRadius)
        .attr("y", -networkSettings.nodeRadius)
        .attr("height", networkSettings.nodeRadius * 2)
        .attr("width", networkSettings.nodeRadius * 2)
        .attr("xlink:href", function (d) {
            return `/images/entity-${getNodeData(d).type}.svg`;
        });
}

function labelHtml(nodeData, networkSettings, extraNode) {
    let tags = ""
    if (nodeData.tags !== undefined) {
        nodeData.tags.forEach(function (tag) {
            tags += `<a href="${networkSettings.tagUrl(nodeData, tag)}" target="_blank" class="tag ${tag.color.toLowerCase()}">${tag.label}</a>`;
        });
    }

    let label = nodeData.showLink
        ? `<a href="${networkSettings.nodeUrl(nodeData)}" target="_blank">${nodeData.name}</a>`
        : `<div>${nodeData.name}</div>`;

    return `<div class="label ${extraNode && extraNode.relatedTo === nodeData.id ? 'left-of-circle' : ''}">
                <div class="text">${label}</div>
                <div class="tags">${tags}</div>
           </div>`;
}

function addNodeLabel(root, node, networkSettings, extraNode) {
    node.append("foreignObject")
        .attr("transform", function (d) {
            if (extraNode && d.data.id === extraNode.relatedTo) {
                // When we have an extra node then the node that the extra one is related to needs to have the text to the left (instead of to the right)
                return `translate(${-networkSettings.nodeLabelWidth - networkSettings.nodeRadius},${-networkSettings.nodeRadius - networkSettings.textMargin})`;
            } else if (!d.parent) {
                // This is the root node. We place the label above/below the node, depending on whether it's upside down or not
                return root.upsideDown
                    ? `translate(${-networkSettings.nodeRadius},${networkSettings.nodeRadius})`
                    : `translate(${-networkSettings.nodeRadius},${-networkSettings.nodeRadius * 3 - networkSettings.textMargin})`;
            } else {
                return `translate(${networkSettings.nodeRadius},${-networkSettings.nodeRadius - networkSettings.textMargin})`;
            }
        })
        .attr("width", networkSettings.nodeLabelWidth - networkSettings.textMargin)
        .attr("height", networkSettings.nodeRadius * 2 + networkSettings.textMargin * 2)
        .html(function (d) {
            let nodeData = d.data;
            return labelHtml(nodeData, networkSettings, extraNode);
        });
}

function nodeClass(nodeData, isRoot) {
    let cssClass = `node ${nodeData.type.toLowerCase()}`;
    if (nodeData.highlighted) {
        cssClass += " highlighted";
    }
    if (isRoot) {
        cssClass += " root";
    }
    return cssClass;
}

function pathToRoot(d) {
    let current = d.parent;
    let path = [d];
    while (current != null) {
        path.push(current);
        current = current.parent
    }
    return path;
}

function linkInPath(path, link) {
    let childIndex = path.indexOf(link.data.id);
    let parentIndex = path.indexOf(link.parent.data.id);
    return childIndex >= 0 && parentIndex === childIndex + 1;
}

function relationshipText(relationship, concatDetails = false, cssClass = "") {
    let details = relationship.details;
    if (details !== undefined) {
        let detailsContent = concatDetails
            ? `<div class="relationship-text">${details.join("; ")}</div>`
            : details.map(detail => `<div class="relationship-text">${detail}</div>`).join("");
        return `<div class="relationship-text-container ${cssClass}">${detailsContent}</div>`
    }
}

exports.drawNodes = function (root, nodesG, nodes, networkSettings, extraNode) {
    let nodeEnter = nodesG
        .data(nodes.descendants())
        .enter();

    let nodeG = nodeEnter
        .append("g")
        .attr("class", d => nodeClass(d.data, !d.parent))
        .attr("transform", d => `translate(${root.computeX(d.x)},${root.computeY(d.y)})`)

    let circleG = nodeG.append("g").attr("class", "node-circle");

    if (networkSettings.highlightOnHover) {
        circleG
            .on('mouseover', function (event, d) {
                let path = pathToRoot(d).map(d => d.data.id);
                d3.selectAll("#arrow").classed("blurred", true);
                d3.selectAll(".node").classed("blurred", node => !path.includes(node.data.id));
                d3.selectAll(".relationship").classed("blurred", link => !linkInPath(path, link));
                d3.selectAll(".node").classed("hovered", node => path.includes(node.data.id));
                d3.selectAll(".relationship").classed("hovered", link => linkInPath(path, link));
            })
            .on('mouseout', function () {
                d3.selectAll("#arrow").classed("blurred", false);
                d3.selectAll(".node").classed("blurred", false);
                d3.selectAll(".relationship").classed("blurred", false);
                d3.selectAll(".node").classed("hovered", false)
                d3.selectAll(".relationship").classed("hovered", false);
            });
    }

    addNodeIcon(circleG, networkSettings, function (node) {
        return node.data;
    });
    addNodeLabel(root, nodeG, networkSettings, extraNode);
    return nodeEnter;
};

exports.drawLinks = function (g, root, relationships) {
    let links = g.selectAll(".relationship")
        .data(root.descendants().slice(1))
        .enter();

    let relationshipG = links.append("g")
        .attr("class", "relationship");

    relationshipG.append("path")
        .attr("d", function (d) {
            let c = d.data.linkCoordinates;
            return `M ${c.fromX} ${c.fromY} L ${c.intermediateX} ${c.intermediateY} L ${c.toX} ${c.toY} `
        })
        .attr('marker-end', 'url(#arrow)')

    relationshipG.append("foreignObject")
        .attr("transform", function (d) {
            let box = d.data.relationshipTextBox;
            return `translate(${box.x}, ${box.y})`;
        })
        .attr("width", d => d.data.relationshipTextBox.width)
        .attr("height", d => d.data.relationshipTextBox.height)
        .html(function (d) {
            let relationship = relationships.find(rel => rel.parentId === d.parent.data.id && rel.childId === d.data.id);
            return relationshipText(relationship, false, d.data.linkCoordinates.linkType());
        });
};


exports.reverseRelationships = function (relationships) {
    return relationships.map(function (relationship) {
        return {
            parentId: relationship.childId,
            childId: relationship.parentId,
            details: relationship.details
        };
    })
};

exports.emptyNetwork = function (containerSelector, emptyText) {
    let svg = addSvg(containerSelector);
    let g = svg.append("g");
    g.append("foreignObject")
        .attr("x", 30)
        .attr("y", 30)
        .attr("width", 500)
        .attr("height", 100)
        .html(function (d) {
            return `<div>${emptyText}</div>`
        });
};

exports.nodeClass = nodeClass;
exports.addNodeIcon = addNodeIcon;
exports.labelHtml = labelHtml;
exports.relationshipText = relationshipText;
