$ ->

  ws = new WebSocket $("body").data("ws-url")

  ws.onmessage = (event) ->

    js = JSON.parse(event.data);
    console.log(event.data)
    switch js.type
        when "xbee"
            display(js.message)
        when "rfid"
            console.log(js.message)
            displayRfidMsg(js.message)
        when "teamWithCount"
            displayTeamWithCount(js.message)
        when "angle"
            displayAngle(js.message)

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


    console.log("message is" +message.angle.x)
    plot = $("#" + "placeholder").data("plot")
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

  displayTeamWithCount = (msg) ->
    teams = msg.teams
    RED_TEAM = "red"
    BLUE_TEAM = "blue"
    SINGLE_TEAM = "single"
    for i in [0...teams.length]
        switch teams[i].team
            when RED_TEAM
                console.log("RED TEAM HERE")
                $("#red_team").text(teams[i].count)
                $("#red_team_time").text(getFormattedTimeString(teams[i].time))

            when BLUE_TEAM
                $("#blue_team").text(teams[i].count)
                $("#blue_team_time").text(getFormattedTimeString(teams[i].time))

            when SINGLE_TEAM
                $("#single_team").text(teams[i].count)
                $("#single_team_time").text(getFormattedTimeString(teams[i].time))

  getFormattedTimeString = (time) ->
    mins = getMins(time)
    seconds = getSeconds(time)
    milliseconds = getMilliSeconds(time)
    formattedString = mins + ":" + seconds + ":" + milliseconds

  getMins = (milliseconds) ->
     v = parseInt((milliseconds / 60000) % 60) + ""
     if (v.length < 2) then "0" + v else v

  getSeconds = (milliseconds) ->
    v = parseInt((milliseconds / 1000) % 60) + "";
    if (v.length < 2) then "0" + v else v

  getMilliSeconds = (milliseconds) ->
    v = parseInt(milliseconds % 1000) + ""
    if (v.length == 1) then "00" + v
    else if (v.length == 2) then  "0" + v
    else  v