$(document).ready(function () {
    $(".description" ).each(function( index ) {
        $(this).html($(this).text());
    });

    //banner click
    $('.banner img').click(function() {
        var el = $('.popular');
        $('body').animate(
            {scrollTop: $(el).offset().top},
            {duration: 1000, easing: "swing"}
        );
        return false;
    });

    //scroll
    $('li a[href^="#"]').click(function() {
        var el = $(this).attr('href');
        $('body').animate(
            {scrollTop: $(el).offset().top},
            {duration: 1500, easing: "swing"}
        );
        return false;
    });

    //backtotop
    $('.backtotop').click(function(){
       $('html, body').animate({scrollTop:0}, 'slow');
    });
    var scrollDiv=$('a.backtotop');
    $(window).scroll(function() {
        if($(window).scrollTop() == "0") {
            $(scrollDiv).fadeOut("slow");
        } else {
            $(scrollDiv).fadeIn("slow");
        }
    });
    $(scrollDiv).hide();

    //popups
    $('.cd-popup-trigger').on('click', function(event) {
        event.preventDefault();

        $(".add-to-cart input[type='checkbox']").prop("checked", false );
        var id = $(event.target).attr('data-product-id');
        $(".add-to-cart input[name='productId']").val(id);
        $(".add-to-cart .popup-row").attr('id', id);
        $(".add-to-cart .product-name").text($(event.target).attr('data-product-name'));
        $(".add-to-cart .number").text("1");
        $(".add-to-cart .unit").text($(event.target).attr('data-unit'));
        var price = $(event.target).attr('data-price');
        $(".add-to-cart .price-value").text(price);
        $(".add-to-cart .popup-row").attr('data-price', price);
        $('.cd-popup.add-to-cart').addClass('is-visible');
    });
    $('.cart-popup-trigger').on('click', function(event) {
        event.preventDefault();

        $('body').append("<div id='cartContentTemplateDiv'/>");
        $("#cartContentTemplateDiv").load("/assets/views/cart.html", function (response, status, xhr) {
            if (status == "error") {
                console.log("Error loading cart template" + xhr.status + " " + xhr.statusText);
                return;
            }
            $.ajax({
                type: 'GET',
                url: '/cart',
                success: function (result) {
                    console.log(result)
                    if (result.totalCount === 0) {
                        return;
                    }
                    var source = $("#cartContentTemplate").html();
                    var template = Handlebars.compile(source);
                    $('.cd-popup.cart').html(template(result));
                    $(".cd-popup.cart .description" ).each(function( index ) {
                        $(this).html($(this).text());
                    });
                    $('.cd-popup.cart').addClass('is-visible');
                    $('.cart .spinner .btn').on('click', modifyCart);
                }
            });
        });
    });

    function modifyCart(event) {
        var row = $(this).parent().parent();
        var productId = row.attr('data-product-id');
        var entryNo = row.attr('data-entry-no');
        var quantitySpan = $('div[data-product-id="' + productId + '"] .quantity .number');
        var quantity = Number(quantitySpan.text()) + ($(this).hasClass('btn-plus')? 1 : -1);
        if (quantity === 0)
            quantity = 1;
        $.ajax({
            type: 'PUT',
            url: '/cart',
            data: {entryNo: entryNo, quantity: quantity},
            success: function (result) {
                var source = $("#cartContentTemplate").html();
                var template = Handlebars.compile(source);
                $('.cd-popup.cart').html(template(result));
                $(".cd-popup.cart .description" ).each(function( index ) {
                    $(this).html($(this).text());
                });
                $('.cd-popup.cart').addClass('is-visible');
                $('.cart .spinner .btn').on('click', modifyCart);
                $(".cart-total").text(result.totalPrice);
            },
            error: function (xhr, str) {
                console.log('Возникла ошибка: ' + xhr.responseCode);
            }
        });
    }


    //close popup
    $('.cd-popup').on('click', function(event) {
        if( $(event.target).is('.cd-popup-close img') || $(event.target).is('.cd-popup') ) {
            event.preventDefault();
            $(this).removeClass('is-visible');
        }
    });
    //close popup when clicking the esc keyboard button
    $(document).keyup(function(event) {
        if(event.which == '27'){
            $('.cd-popup').removeClass('is-visible');
        }
    });

    $('.add-to-cart .spinner .btn').on('click', function(event) {
        var row = $(this).parent().parent();
        var productId = row.attr('id');
        var quantitySpan = $('#' + productId + ' .quantity .number');
        var res = Number(quantitySpan.text()) + ($(this).hasClass('btn-plus')? 1 : -1);
        if (res === 0)
            res = 1;
        quantitySpan.text(res);
        var price = row.attr('data-price');
        $('#' + productId + ' .price-value').text(price * res);
    });

    $('.box label').on('click', function(event) {
        var cb = $('input#' + $(this).attr('for') + '[type="checkbox"]');
        cb.attr('checked', !cb.attr('checked'));
    });

    $('.add-to-cart .place-btn').on('click', function(event) {
        $('.cd-popup.add-to-cart').removeClass('is-visible');
        $('.cd-popup.cart').addClass('is-visible');
    });

    $('.add-to-cart .add-continue').on('click', function(event) {
        $('.cd-popup.add-to-cart').removeClass('is-visible');

        var msg = $('.add-to-cart form').serializeArray();
        var obj = {additions: []};
        for(var i in msg) {
            var fieldName = msg[i].name;
            if (fieldName == 'additions') {
                obj.additions.push(msg[i].value);
            } else {
                obj[fieldName] = msg[i].value;
            }
        }
        obj.quantity = Number($('.add-to-cart .number').text());
        if (isNaN(obj.quantity)) {
            return;
        }
        $.ajax({
            type: 'POST',
            url: '/cart',
            data: obj,
            error: function (xhr, str) {
                console.log('Возникла ошибка: ' + xhr.responseCode);
            }
        }).always(function () {
            updateCart();
        });
    });


    function updateCart() {
        $.ajax({
            type: 'GET',
            url: '/cart',
            success: function (result) {
                $(".cart-total").text(result.totalPrice);
            }
        });
    }



});


function removeFromCart(entryNo) {
        $.ajax({
            type: 'DELETE',
            url: '/cart?entryNo=' + entryNo,
            error: function (xhr, str) {
                console.log('Возникла ошибка: ' + xhr.responseCode);
            }
        }).always(function () {
            $.ajax({
                type: 'GET',
                url: '/cart',
                success: function (result) {
                    var source = $("#cartContentTemplate").html();
                    var template = Handlebars.compile(source);
                    $('.cd-popup.cart').html(template(result));
                    $(".cd-popup.cart .description" ).each(function( index ) {
                        $(this).html($(this).text());
                    });
                    $('.cd-popup.cart').addClass('is-visible');
                    $('.cart .spinner .btn').on('click', modifyCart);
                    $(".cart-total").text(result.totalPrice);
                }
            });
        });
    }

function changeInputValue(id, delta) {
    var input = $('#' + id);
    var val = input.val();
    try {
        val = parseInt(val.match(/\d+/)[0]) + delta;
    } catch (err) {
        val = NaN;
    }
    if (val < 1 || isNaN(val))
        val = 1;
    val = val + " кг";
    input.val(val);
}





function onDeliveryClick(active, nonactive) {
    $('#' + active).addClass('active');
    $('#' + nonactive).removeClass('active');
    $('[name="delivery"]').val(active);
    if (active === 'freeDelivery') {
        $('[name="deliveryDate"]').attr('disabled', '').val('NULL');
        $('[name="deliveryTime"]').attr('disabled', '').val('NULL');
    } else {
        $('[name="deliveryDate"]').removeAttr('disabled');
        $('[name="deliveryTime"]').removeAttr('disabled');
    }
}


