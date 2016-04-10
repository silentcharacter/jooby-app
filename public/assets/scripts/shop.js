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

});