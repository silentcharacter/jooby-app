$(document).ready(function () {
  $('[data-toggle="offcanvas"]').click(function () {
    $('.row-offcanvas').toggleClass('active')
  });

  $('[data-toggle="popover"]').popover({
    html : true,
    content: function() {
      return $('#popover_content_wrapper').html();
    }
  });
  $('#addToCart').click(function () {
        var msg   = $('#addToCartForm').serialize();
        $.ajax({
          type: 'POST',
          url: '/addToCart',
          data: msg,
          success: function(data) {
            console.log(data);
          },
          error:  function(xhr, str){
            console.log('Возникла ошибка: ' + xhr.responseCode);
          }
        });
  });
});

function onOrderClick(id) {
    $("input[name='productId'").val(id);
}