$(document).ready(function () {
    $('[data-toggle="offcanvas"]').click(function () {
        $('.row-offcanvas').toggleClass('active')
    });

    setUpPopover();

    $("[name='phone']").mask("0000000000");

    $('#street').typeahead({
        source: function (query, process) {
            return $.ajax({
                 type: 'POST',
                 url: 'https://dadata.ru/api/v1/suggest/address',
                 headers: {
                     "Content-Type": "application/json",
                     "Authorization": "Token bf69a05b6ce842dcd0cbc159648d19a8c49fdf33"
                 },
                 data: JSON.stringify({"query": "Ярославль " + query}),
                 success: function(result) {
                   var suggestions = [];
                   for (var s of result.suggestions) {
                     suggestions.push(s.data.street_with_type);
                   }
                   return process(suggestions);
                 }
           });
        }
    });


});

function setUpPopover() {
    $('[data-toggle="popover"]').popover({
        html : true,
        content: function() {
          return $('#popover_content_wrapper').html();
        }
      }
    );
}

function updateCart() {
    $.ajax({
      type: 'GET',
      url: '/cart',
      success: function(result) {
        $("#cartPlaceholder").html(result);
        setUpPopover();
      }
    });
}

function onOrderClick(id) {
    $("input[name='productId'").val(id);
}

function onHideCartClick() {
    $('[data-toggle="popover"]').trigger('click');
}

function addToCart() {
    var msg = $('#addToCartForm').serialize();
    $.ajax({
      type: 'POST',
      url: '/addToCart',
      data: msg,
      error:  function(xhr, str){
        console.log('Возникла ошибка: ' + xhr.responseCode);
      }
    }).always(function(){
        updateCart();
    });
}

function removeFromCart(entryNo) {
    $.ajax({
      type: 'POST',
      url: '/removeFromCart?entryNo=' + entryNo,
      error:  function(xhr, str){
        console.log('Возникла ошибка: ' + xhr.responseCode);
      }
    }).always(function(){
      updateCart();
    });
}

function onDeliveryClick(active, nonactive) {
    $('#' + active).addClass('active');
    $('#' + nonactive).removeClass('active');
    $('[name="delivery"]').val(active);
    if (active === 'freeDelivery') {
        $('[name="date"]').attr('disabled', '').val('NULL');
        $('[name="time"]').attr('disabled', '').val('NULL');
    } else {
        $('[name="date"]').removeAttr('disabled');
        $('[name="time"]').removeAttr('disabled');
    }
}


