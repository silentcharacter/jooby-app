$(document).ready(function () {
    $('[data-toggle="offcanvas"]').click(function () {
        $('.row-offcanvas').toggleClass('active')
    });

    setUpPopover();
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
