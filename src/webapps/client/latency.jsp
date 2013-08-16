<!DOCTYPE html>

<head>
    <meta charset="utf-8">
    <link rel="stylesheet" href="/static/histogram.css"/>
    <link rel="stylesheet" href="/static/base.css"/>
</head>

<body>
    <script type="text/javascript" src="/static/d3.v3.min.js"></script>
    <script type="text/javascript">
        var histo = JSON.parse('<%= request.getAttribute("latency") %>');
        var xTag = histo.xTag;
        var yTag = histo.yTag;

        var margin = { top: 20, right: 60, bottom: 100, left: 60 };
        var width = 960 - margin.left - margin.right;
        var height = 550 - margin.top - margin.bottom;

        var x = d3.scale.log().base(10).range([0, width]);
        var y = d3.scale.linear().range([height, 0]);

        var xAxis = d3.svg.axis()
            .scale(x)
            .orient("bottom")
            .ticks(1, d3.format(",.0f"))
            .tickSubdivide(0);
        var yAxis = d3.svg.axis()
            .scale(y)
            .orient("left");

        var body = d3.select("body");
        var svg = body.append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + ", " + margin.top + ")");

        var xMin = histo.bins[0].lower;
        var xMax = histo.bins[histo.bins.length - 1].upper;
        var yMax = d3.max(histo.bins, function(f) { return f.count; });

        x.domain([xMin, xMax]);
        y.domain([0, yMax]);

        svg.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(" + 0 + "," + height + ")")
            .call(xAxis)
            .append("text")
            .attr("x", width - 10)
            .attr("y", 20)
            .attr("dy", ".71em")
            .attr("text-anchor", "end")
            .text(xTag);

        svg.append("g")
            .attr("class", "y axis")
            .call(yAxis)
            .append("text")
            .attr("transform", "rotate(-90)")
            .attr("y", 6)
            .attr("dy", ".71em")
            .style("text-anchor", "end")
            .text(yTag);

        // draw bars
        svg.selectAll(".bar")
            .data(histo.bins)
            .enter().append("rect")
            .attr("class", "bar")
            .attr("x", function(d) { return x(d.lower); })
            .attr("width", function(d) { return x(d.upper) - x(d.lower); })
            .attr("y", function(d) { return y(d.count); })
            .attr("height", function(d) { return height - y(d.count); });

        // draw percentiles
        var percentileVis = svg.append("g")
            .attr("class", "percentile")
            .attr("transform", "translate(" + 0 + "," + height + ")");
        histo.percentiles.forEach(function(percentile) {
            if (percentile.position == null) {
                return;
            }
            var x1 = x(percentile.position);
            var x2 = width * percentile.percent;
            percentileVis.append("line")
                .attr("x1", x1)
                .attr("x2", x1)
                .attr("y1", 0)
                .attr("y2", 10);
            percentileVis.append("line")
                .attr("x1", x1)
                .attr("y1", 10)
                .attr("x2", x2)
                .attr("y2", 50);
            percentileVis.append("circle")
                .attr("cx", x2)
                .attr("cy", 50)
                .attr("r", 3);
            percentileVis.append("text")
                .attr("x", x2)
                .attr("y", 50)
                .attr("dx", "-1.5em")
                .attr("dy", "1.5em")
                .text(percentile.percent * 100 + "%:" + percentile.position);
        });

        // draw summary table
        var intFormat = d3.format(',.0f');
        var summaryInfo = [
            { name: "Total Count", value: intFormat(histo.totalCount) },
            { name: "Average", value: intFormat(histo.average) },
            { name: "Minimum", value: intFormat(histo.min) },
            { name: "Maximum", value: intFormat(histo.max) }];

        var summaryTable = body.append("div")
            .attr("class", "summary")
            .append("table");
        summaryTable.append("th")
            .append("text")
            .text("Summary");
        summaryInfo.forEach(function(info) {
            var tableRow = summaryTable.append("tr");
            tableRow.append("td")
                .append("text").text(info.name);
            tableRow.append("td")
                .append("text").text(info.value);
        });
    </script>
</body>


