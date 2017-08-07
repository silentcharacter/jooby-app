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
    var scrollDiv=$('a.idTop');
    $(window).scroll(function() {
        if($(window).scrollTop() == "0") {
            $(scrollDiv).fadeOut("slow");
        } else {
            $(scrollDiv).fadeIn("slow");
        }
    });
    $(scrollDiv).hide();

    //popup
    $('.cd-popup-trigger').on('click', function(event){
        event.preventDefault();
        $('.cd-popup').addClass('is-visible');
    });

    //close popup
    $('.cd-popup').on('click', function(event){
        if( $(event.target).is('.cd-popup-close') || $(event.target).is('.cd-popup') ) {
            event.preventDefault();
            $(this).removeClass('is-visible');
        }
    });
    //close popup when clicking the esc keyboard button
    $(document).keyup(function(event){
        if(event.which=='27'){
            $('.cd-popup').removeClass('is-visible');
        }
    });

});


function updateCart() {
    $.ajax({
        type: 'GET',
        url: '/cart',
        success: function (result) {
            $("span.cart-count").text(result.totalCount);
        }
    });
}

function modifyCart(entryNo, quantity) {
    $.ajax({
        type: 'PUT',
        url: '/cart',
        data: {entryNo: entryNo, quantity: quantity},
        success: function (result) {
            var template = initHandlebarsCartTemplate();
            $('#cartContent').html(template(result));
            $("span.cart-count").text(result.totalCount);
        },
        error: function (xhr, str) {
            console.log('Возникла ошибка: ' + xhr.responseCode);
        }
    });
}

function onAddToCartBtnClick(id) {
    $('#addToCartForm').find("input[type='checkbox']").prop( "checked", false );
    $('#addToCartForm').find("input[type='radio']").prop( "checked", false );
    $('#addToCartForm').find("input[type='radio']:first").prop( "checked", true );
    $("input[name='productId']").val(id);
    $("input[name='quantity']").val("1 кг");
    $('#addToCartModal').modal('show');
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

function addToCart() {
    var msg = $('#addToCartForm').serializeArray();
    var obj = {sauces: []};
    for(var i in msg) {
        var fieldName = msg[i].name;
        if (fieldName == 'sauces') {
            obj.sauces.push(msg[i].value);
        } else {
            obj[fieldName] = msg[i].value;
        }
    }
    obj.quantity = parseInt(obj.quantity);
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
        onCartBtnClick();
    });
}

function removeFromCart(entryNo) {
    $.ajax({
        type: 'DELETE',
        url: '/cart?entryNo=' + entryNo,
        error: function (xhr, str) {
            console.log('Возникла ошибка: ' + xhr.responseCode);
        }
    }).always(function () {
        updateCart();
    });
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

function initHandlebarsCartTemplate() {
    Handlebars.registerHelper("math", function (lvalue, operator, rvalue, options) {
        lvalue = parseFloat(lvalue);
        rvalue = parseFloat(rvalue);

        return {
            "+": lvalue + rvalue,
            "-": lvalue - rvalue,
            "*": lvalue * rvalue,
            "/": lvalue / rvalue,
            "%": lvalue % rvalue
        }[operator];
    });
    Handlebars.registerHelper('if_eq', function(a, b, opts) {
        if(a == b)
            return opts.fn(this);
        else
            return opts.inverse(this);
    });

    var source = $("#cartContentTemplate").html();
    return Handlebars.compile(source);
}
function onCartBtnClick() {
    $('body').append("<div id='cartContentTemplateDiv'/>");
    $("#cartContentTemplateDiv").load("/assets/views/cart.html", function (response, status, xhr) {
        if (status == "error") {
            console.log("Error loading cart template" + xhr.status + " " + xhr.statusText);
            return;
        }
        console.log("Load was performed.");

        $.ajax({
            type: 'GET',
            url: '/cart',
            success: function (result) {
                console.log(result)
                if (result.totalCount === 0) {
                    return;
                }
                var template = initHandlebarsCartTemplate();
                $('#cartContent').html(template(result));
                $('#cartModal').modal('show')
            }
        });
    });

}
