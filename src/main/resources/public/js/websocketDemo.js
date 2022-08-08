
$(document).ready(function () {

    var table; 
    var dataSet = [ ];
    
    $(document).ready(function () {
        table = $('#example').DataTable({
            data: dataSet,
            columns: [
                {title: "Symbol", data: "symbol"},
                {title: "Price", data: "price"}
            ]
        });
    });

    //Establish the WebSocket connection and set up event handlers
    let ws = new WebSocket("ws://" + location.hostname + ":" + location.port + "/tickerfeed");
    ws.onmessage = message => updateTable(message);
    ws.onclose = () => alert("WebSocket connection closed");

    function updateTable(message) {
        let stockData = JSON.parse(message.data);        
        // check if symbol already in table:
        var index = table.column( 0, {order:'index'} ).data()
                .indexOf( stockData.symbol );        
        if (index >= 0) {
            // update the existing row:
            table.row( index, {order:'index'} ).data( stockData ).draw();
        } else {
            // insert a new row:
            table.row.add( stockData ).draw();
        }

    }

});