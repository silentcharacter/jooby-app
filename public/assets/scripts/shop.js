SHOP = {
    bindBannerClick: function() {
        //banner click
        $('.banner img').click(function() {
            var el = $('.popular');
            $('body').animate(
                {scrollTop: $(el).offset().top - 50},
                {duration: 1000, easing: "swing"}
            );
            return false;
        });
    },

    bindPopupEvents: function() {
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
            SHOP.showCart();
        });

        //close popup
        $('.cd-popup').on('click', function(event) {
            if ($(event.target).is('.cd-popup-close img') || $(event.target).is('.cd-popup') ) {
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

        $('.add-to-cart-js .spinner .btn').on('click', function(event) {
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

        $(".product-detail-description input[type='checkbox']").prop("checked", false );
    },

    showCart: function() {
        if ($('#cartContentTemplateDiv').length === 0)
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

                    var container = $('.cart .cd-popup-container');
                    var offset = ($(window).height() - container.height()) / 2;
                    container.css('margin-top', offset + 'px');
                    container.css('margin-bottom', offset + 'px');
                    if (($(window).height() - container.height()) / $(window).height() < 0.12) {
                        container.css('margin-top', '4%');
                        container.css('margin-bottom', '4%');
                        container.css('height', '85%');
                        $('.cart-entries').css('max-height', (container.height() - 200) + 'px');
                    }
                    $('.cd-popup.cart').addClass('is-visible');
                    $('.cart .spinner .btn').on('click', SHOP.modifyCart);
                }
            });
        });
    },

    updateCartTotal: function() {
        $.ajax({
            type: 'GET',
            url: '/cart',
            success: function (result) {
                $(".cart-total").text(result.totalPrice);
            }
        });
    },

    modifyCart: function(event) {
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
                //todo: optimize double call
                SHOP.updateCartTotal();
                SHOP.showCart();
            },
            error: function (xhr, str) {
                console.log('Возникла ошибка: ' + xhr.responseCode);
            }
        });
    },

    formatDescriptions: function() {
        $(".description" ).each(function( index ) {
            $(this).html($(this).text());
        });
    },

    bindFixedMenu: function() {
        $(window).scroll(function(){
            if ($(window).width() < 850) {
                elem.css('display', 'none');
                return;
            }
            var top = $(this).scrollTop();
            var elem = $('.fixed-nav-bar');
            var header = $('header');
            var nav = $('.main-navigation');
            if (top < header.height() + nav.height()) {
                elem.css('display', 'none');
            } else {
                elem.css('display', 'block');
            }
        });
    },

    bindScrolls: function() {
        //scroll
        $('li a[href*="#"]').click(function(e) {
            var el = '#' + $(this).attr('href').split('#')[1];;
            $('body').animate(
                {scrollTop: ($(el).offset().top - 20)},
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
    }
}

$(document).ready(function () {
    SHOP.bindBannerClick();
    SHOP.formatDescriptions();
    SHOP.bindPopupEvents();
    SHOP.bindScrolls();
    SHOP.bindFixedMenu();
});


function removeFromCart(entryNo) {
    $.ajax({
        type: 'DELETE',
        url: '/cart?entryNo=' + entryNo,
        error: function (xhr, str) {
            console.log('Возникла ошибка: ' + xhr.responseCode);
        }
    }).always(function () {
        $('.cd-popup.cart').removeClass('is-visible');
        //todo: optimize double call
        SHOP.updateCartTotal();
        SHOP.showCart();
    });
}

function addAndPlace() {
    addToCart(true);
}

function addAndContinue() {
    addToCart(false);
}

function addToCart(openCart) {
    $('.cd-popup.add-to-cart').removeClass('is-visible');

    var msg = $('.add-to-cart-js form').serializeArray();
    var obj = {additions: []};
    for(var i in msg) {
        var fieldName = msg[i].name;
        if (fieldName == 'additions') {
            obj.additions.push(msg[i].value);
        } else {
            obj[fieldName] = msg[i].value;
        }
    }
    obj.quantity = Number($('.add-to-cart-js .number').text());
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
        SHOP.updateCartTotal();
        if (openCart) {
            SHOP.showCart();
        }
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


