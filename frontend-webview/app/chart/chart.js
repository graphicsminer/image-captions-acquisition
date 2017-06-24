$(document).ready(function() {
    var token = _helper.getToken();
    var loggedUser, images = [];

    var groupedCategories = {};


    // check whether user is exit or not. If not -> redirect to login page
    $.ajax({
        async: true,
        crossDomain: true,
        url: _config.api_user_get + '?_id=' + _helper.getLoggedUser() + '&token=' + token,
        type: "GET"

    }).done(function(res) {
        if (!(res instanceof Array) || res[0] == null) {
            // Notify error message
            return _helper.error();
        }

        // OK -> 
        loggedUser = new User(res[0]);

        // alow all users to view chart
        // if (!loggedUser.hasRoles(['admin', 'manager'])) {
        //     // redirect to login page
        //     window.location.href = "../login/login.html";
        // }

        // get all images        
        $.ajax({
            async: true,
            crossDomain: true,
            url: _config.api_image_get + '?token=' + token,
            type: "GET"

        }).done(function(res) {
            if (!(res instanceof Array)) {
                // Notify error message
                return _helper.error();
            }
            images = res;

            // draw contributor pie chart
            drawContributorPieChart();
            drawContributorBarChart();

            // draw category bar chart
            $.ajax({
                async: true,
                crossDomain: true,
                url: _config.api_resource_config + 'categories-group-vi_VN.json?token=' + token,
                type: "GET"
            }).done(function(res) {

                groupedCategories = res;

                $('#categoryThreshold').val(_helper.getDefaultCategoryThreshold());
                $('#categoryGrouped').attr('checked', _helper.isCategoryGroupped());

                drawCategoriesBarChart({});

                $('#categoryGrouped').change(onCategoryGrouped);
                $('#categoryThresholdRefreshBtn').click(onRefreshCategoriesBarChart);
                $('#categorySortBtn').click(onCategorySorted);

            }).fail(function(res) {
                // redirect to login page
                window.location.href = "../login/login.html";
            });

            // draw status pie chart
            drawStatusPieChart();

        }).fail(function(res) {});


    }).fail(function(res) {
        // redirect to login page
        window.location.href = "../login/login.html";
    });

    /**
     * Visualize the number of images contributed by contributors in pie chart
     */
    function drawContributorPieChart() {
        var data = [],
            map = {},
            colors = _helper.getColors();

        images.forEach(function(item) {
            let image = new Image(item);
            let user = image.get('contributor');

            if (!map[user]) map[user] = 0;
            map[user]++;
        });

        let idx = 0;
        for (let key in map) {
            data.push({
                "label": key,
                "value": map[key],
                "color": colors[idx]
            });
            idx++;
        }

        var pie = new d3pie("contributorPieChart", {
            "header": {
                "title": {
                    "text": "List of Contributors",
                    "fontSize": 24,
                    "font": "open sans"
                },
                "subtitle": {
                    "text": "Total images: " + images.length,
                    "color": "#999999",
                    "fontSize": 12,
                    "font": "open sans"
                },
                "titleSubtitlePadding": 9
            },
            "footer": {
                "color": "#999999",
                "fontSize": 10,
                "font": "open sans",
                "location": "bottom-left"
            },
            "size": {
                "canvasWidth": 590,
                "pieOuterRadius": "90%"
            },
            "data": {
                "sortOrder": "value-desc",
                "content": data
            },
            "labels": {
                "outer": {
                    "pieDistance": 32
                },
                "inner": {
                    "hideWhenLessThanPercentage": 3
                },
                "mainLabel": {
                    "fontSize": 11
                },
                "percentage": {
                    "color": "#ffffff",
                    "decimalPlaces": 0
                },
                "value": {
                    "color": "#adadad",
                    "fontSize": 11
                },
                "lines": {
                    "enabled": true
                },
                "truncation": {
                    "enabled": true
                }
            },
            "effects": {
                "pullOutSegmentOnClick": {
                    "effect": "linear",
                    "speed": 400,
                    "size": 8
                }
            },
            "misc": {
                "gradient": {
                    "enabled": true,
                    "percentage": 100
                }
            },
            "tooltips": {
                "enabled": true,
                "type": "placeholder",
                "string": "{label}: {value}, {percentage}%"
            },
        });
    }

    /**
     * Visualize the number of images contributed by contributors in bar chart
     */
    function drawContributorBarChart() {
        var data = [],
            map = {}, // image by user and status
            statuses = [],
            imageByStatus = {}, // image by status
            colors = _helper.getColors();

        // classify image by user and status
        images.forEach(function(item) {
            let image = new Image(item);
            let user = image.get('contributor');
            let status = image.get('status');

            if (statuses.indexOf(status) == -1) {
                statuses.push(status);
            }

            // get image by status
            if (!imageByStatus[status]) imageByStatus[status] = 0;
            imageByStatus[status] += 1

            // image by user and status
            if (!map[user]) map[user] = {};
            if (!map[user][status]) map[user][status] = 0;
            map[user][status]++;
        });

        // create dataset        
        for (let key in map) {
            let values = [];
            for (let status in map[key]) {
                values.push({
                    'status': status,
                    'value': map[key][status]
                });
            }

            data.push({
                "label": key,
                "value": values,
            });
        }

        var margin = { top: 40, right: 30, bottom: 80, left: 40, legend: 60 },
            width = $(document).width() / 2 - margin.left - margin.right,
            height = 500 - margin.top - margin.bottom;

        var x0 = d3.scale.ordinal()
            .rangeBands([0, width], .2)
            .domain(data.map(function(d) {
                return d.label;
            }));

        var x1 = d3.scale.ordinal()
            .rangeBands([0, x0.rangeBand()])
            .domain(statuses);

        var z = d3.scale.category10();
        var colors = {};
        statuses.forEach(function(status, i) {
            colors[status] = z(i);
        });
        // hard code for color of (DONE, ACCEPT and TODO)
        colors['DONE'] = '#1F77B4';
        colors['ACCEPTED'] = '#2CA02C';
        colors['TODO'] = '#FF7F0E';

        var y = d3.scale.linear()
            .range([height, 0])
            .domain([0, d3.max(data, function(d) {
                return d3.max(d.value, function(v) {
                    return v.value;
                });
            })])
            .nice();

        var svg = d3.select("#contributorBarChart").append("svg")
            .attr("width", width + margin.left + margin.right + margin.legend)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        svg.append("g")
            .attr("transform", "translate(" + (width / 2) + ", -10)")
            .append("text")
            .text(images.length + " Images by Contributors")
            .style({ "text-anchor": "middle", "font-size": "24px", "font-family": "open sans" });

        var tip = d3.tip()
            .attr('class', 'contributor-tip')
            .offset([-10, 0])
            .html(function(d) {
                return "<strong>Images:</strong> <span style='color:red'>" + d.value + "</span>";
            })
        svg.call(tip);

        svg.append("g")
            .attr("class", "x axis contributor")
            .attr("transform", "translate(0," + height + ")")
            .call(d3.svg.axis().scale(x0).orient("bottom"))
            .selectAll("text")
            .attr("y", 0)
            .attr("x", 9)
            .attr("transform", "rotate(60)")
            .style("text-anchor", "start");;

        svg.append("g")
            .attr("class", "y axis")
            .call(d3.svg.axis().scale(y).orient("left"));

        svg.append("g").selectAll("g")
            .data(data)
            .enter().append("g")
            .attr("transform", function(d) {
                return "translate(" + x0(d.label) + ",0)";
            })
            .selectAll(".bar.contributor")
            .data(function(d) {
                return d.value;
            })
            .enter().append("rect")
            .attr("class", "bar category")
            .attr("width", x1.rangeBand())
            .attr("height", function(d) {
                return height - y(d.value);
            })
            .attr("x", function(d, i) {
                return x1(d.status);
            })
            .attr("y", function(d) {
                return y(d.value);
            })
            .style("fill", function(d, i) {
                return colors[d.status];
            })
            .on('mouseover', tip.show)
            .on('mouseout', tip.hide);

        // create legend
        var legend = svg.append("g")
            .attr("font-family", "sans-serif")
            .attr("font-size", 10)
            .attr("text-anchor", "end")
            .selectAll("g")
            .data(statuses)
            .enter().append("g")
            .attr("transform", function(d, i) {
                return "translate(0," + i * 20 + ")";
            });

        legend.append("rect")
            .attr("x", width + margin.right + margin.legend - 19)
            .attr("width", 19)
            .attr("height", 19)
            .style("fill", function(d) {
                return colors[d];
            });

        legend.append("text")
            .attr("x", width + margin.right + margin.legend - 24)
            .attr("y", 9.5)
            .attr("dy", "0.32em")
            .text(function(d) {
                return imageByStatus[d] + " " + d;
            });

    }

    function isMatch(group, categories) {
        for (let i = 0; i < categories.length; i++) {
            if (group.indexOf(categories[i]) !== -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Visualize the number of images by categories in bar chart
     */
    function drawCategoriesBarChart(options) {
        let threshold = options.threshold ? options.threshold : _helper.getDefaultCategoryThreshold();
        let grouped = (options.grouped != undefined) ? options.grouped : _helper.isCategoryGroupped();
        let sortType = options.sortType ? options.sortType : _helper.getCategorySortedType();

        var data = [],
            map = {},
            colors = _helper.getColors();
        // init map for all groiped categories
        if (grouped) {
            for (let key in groupedCategories) {
                map[key] = 0;
            }
        }

        images.filter(function(item) {
            if (item.status == 'DONE' || item.status == "ACCEPTED") {
                return true;
            }
            return false;

        }).forEach(function(item) {
            let image = new Image(item);
            let categories = image.getCategories();
            let vnCategories = categories['@vi_VN'] || [];

            if (grouped) {
                for (let key in groupedCategories) {
                    if (isMatch(groupedCategories[key], vnCategories)) {
                        map[key]++;
                    }
                }
            } else {
                for (let i = 0; i < vnCategories.length; i++) {
                    let cat = vnCategories[i];
                    if (!map[cat]) map[cat] = 0;
                    map[cat]++;
                }
            }

        });

        let idx = 0;
        let other = 0;
        for (let key in map) {
            if (map[key] > threshold) {
                data.push({
                    "label": key,
                    "value": map[key],
                    "color": colors[idx]
                });
                idx++;
            } else {
                other += map[key];
            }
        }

        // sort data
        if (sortType != 'NONE') {
            if (sortType == 'ASC') {
                data.sort(function(a, b) {
                    return b.value - a.value;
                });
            } else /*DESC*/ {
                data.sort(function(a, b) {
                    return a.value - b.value;
                });
            }
        }
        data.push({
            "label": 'Khác',
            "value": other,
            "color": colors[idx + 1]
        });

        var margin = { top: 40, right: 30, bottom: 120, left: 40 },
            width = $(document).width() - margin.left - margin.right,
            height = 600 - margin.top - margin.bottom;

        var x = d3.scale.ordinal()
            .rangeRoundBands([0, width], 0.1, 0.2);

        var y = d3.scale.linear()
            .range([height, 0]);

        var svg = d3.select("#categoriesBarChart").append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        svg.append("g")
            .attr("transform", "translate(" + (width / 2) + ", -10)")
            .append("text")
            .text("Image by Categories")
            .style({ "text-anchor": "middle", "font-size": "24px", "font-family": "open sans" });

        x.domain(data.map(function(d) {
            return d.label;
        }));
        y.domain([0, d3.max(data, function(d) {
            return d.value;
        })]);

        var tip = d3.tip()
            .attr('class', 'category-tip')
            .offset([-10, 0])
            .html(function(d) {
                return "<strong>Images:</strong> <span style='color:red'>" + d.value + "</span>";
            })
        svg.call(tip);

        svg.append("g")
            .attr("class", "x axis category")
            .attr("transform", "translate(0," + height + ")")
            .call(d3.svg.axis().scale(x).orient("bottom"))
            .selectAll("text")
            .attr("y", 0)
            .attr("x", 9)
            .attr("transform", "rotate(60)")
            .style("text-anchor", "start")
            .each(function() {
                let text = d3.select(this);
                _helper.wrap(text, ',');
            });

        svg.append("g")
            .attr("class", "y axis")
            .call(d3.svg.axis().scale(y).orient("left"));

        svg.selectAll(".bar.category")
            .data(data)
            .enter().append("rect")
            .attr("class", "bar category")
            .attr("x", function(d) {
                return x(d.label);
            })
            .attr("width", x.rangeBand())
            .attr("y", function(d) {
                return y(d.value);
            })
            .attr("height", function(d) {
                return height - y(d.value);
            })
            .on('mouseover', tip.show)
            .on('mouseout', tip.hide);
    }

    function onRefreshCategoriesBarChart(e) {
        var thresh = $('#categoryThreshold').val();
        if (!thresh || thresh <= 0) {
            thresh = 5;
        }
        $('#categoriesBarChart').empty();
        _helper.setDefaultCategoryThreshold(thresh);
        drawCategoriesBarChart({ threshold: thresh });
    }

    function onCategoryGrouped(e) {
        let isGrouped = e.target.checked;
        $('#categoriesBarChart').empty();
        _helper.setCategoryGrouped(isGrouped);
        drawCategoriesBarChart({ grouped: isGrouped });
    }

    function onCategorySorted(e) {
        // NONE -> ASC -> DESC -> NONE
        let sortType = _helper.getCategorySortedType();
        switch (sortType) {
            case 'NONE':
                sortType = 'ASC';
                break;
            case 'ASC':
                sortType = 'DESC';
                break;
            case 'DESC':
                sortType = 'NONE';
                break;
        }
        _helper.setCategorySortedType(sortType);
        $('#categoriesBarChart').empty();
        drawCategoriesBarChart({ sortType: sortType });
    }

    /**
     * Visualize the number of images by status
     */
    function drawStatusPieChart() {
        var data = [],
            map = {},
            colors = _helper.getColors();

        images.forEach(function(item) {
            let image = new Image(item);
            let status = image.get('status');

            if (!map[status]) map[status] = 0;
            map[status]++;
        });

        let idx = 0;
        for (let key in map) {
            data.push({
                "label": key,
                "value": map[key],
                "color": colors[idx]
            });
            idx++;
        }

        var pie = new d3pie("statusPieChart", {
            "header": {
                "title": {
                    "text": "Image by Statuses",
                    "fontSize": 24,
                    "font": "open sans"
                },
                "subtitle": {
                    "text": "Total images: " + images.length,
                    "color": "#999999",
                    "fontSize": 12,
                    "font": "open sans"
                },
                "titleSubtitlePadding": 9
            },
            "footer": {
                "color": "#999999",
                "fontSize": 10,
                "font": "open sans",
                "location": "bottom-left"
            },
            "size": {
                "canvasWidth": 590,
                "pieOuterRadius": "90%"
            },
            "data": {
                "sortOrder": "value-desc",
                "content": data
            },
            "labels": {
                "outer": {
                    "pieDistance": 32
                },
                "inner": {
                    "hideWhenLessThanPercentage": 3
                },
                "mainLabel": {
                    "fontSize": 11
                },
                "percentage": {
                    "color": "#ffffff",
                    "decimalPlaces": 0
                },
                "value": {
                    "color": "#adadad",
                    "fontSize": 11
                },
                "lines": {
                    "enabled": true
                },
                "truncation": {
                    "enabled": true
                }
            },
            "effects": {
                "pullOutSegmentOnClick": {
                    "effect": "linear",
                    "speed": 400,
                    "size": 8
                }
            },
            "misc": {
                "gradient": {
                    "enabled": true,
                    "percentage": 100
                }
            },
            "tooltips": {
                "enabled": true,
                "type": "placeholder",
                "string": "{label}: {value}, {percentage}%"
            },
        });
    }
});
