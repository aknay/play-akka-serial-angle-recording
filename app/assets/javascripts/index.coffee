$ ->

  ws = new WebSocket $("body").data("ws-url")

  ws.onmessage = (event) ->
    js = JSON.parse(event.data)
    switch js.type
        when "angle"
            displayAngle(js.message)

  $("#dateform").submit (event) ->
    console.log( $("#date").val())
    # send the date back
    ws.send(JSON.stringify({date: $("#date").val()}))

  ws.onopen = (event) ->
    numberOfSecondInOneDay = 86400
    numberOfSecondInOneMin = 60
    numberOfDataFiredPerSecond = 2
    displayRange = numberOfSecondInOneMin * numberOfDataFiredPerSecond
    d = []
    d.push(0) for i in [0..displayRange]

    chart = $("<div>").addClass("chart").prop("id", "placeholder")
    $("#stocks").prepend(chart)
    plot = chart.plot([getChartArray(d), getChartArray(d)], getChartOptions())

    console.log("opened")

  ws.onclose = (event) ->
    console.log("closed")

  ws.onerror = (event) ->
    console.log("error")

  getChartArray = (data) ->
    ([i, v] for v, i in data)

  getPricesFromArray = (data) ->
    (v[1] for v in data)

  getChartOptions = () ->
    series:
        shadowSize: 0
    yaxis:
        min: -360
        max: 360
    xaxis:
        show: false

  display = (message) ->
    $("#message_holder").text(message)

  displayAngle = (message) ->
    $("#message_holder").text("X angle: " +message.angle.x + ", Y angle: " +message.angle.y)
    plot = $("#" + "placeholder").data("plot")
    if (typeof plot isnt "undefined")
        data = getPricesFromArray(plot.getData()[0].data)
        yData = getPricesFromArray(plot.getData()[1].data)
        console.log(yData)
        yData.shift()
        yData.push(message.angle.y)
        data.shift()
        v = Math.floor((Math.random() * 10) + 1);
        data.push(message.angle.x)
        plot.setData([getChartArray(data), getChartArray(yData)])
        plot.draw()

  displayRfidMsg = (msg) ->
    $("#rfid_field input").val(msg.tag)
    $("#rfid_message").text(msg.time)