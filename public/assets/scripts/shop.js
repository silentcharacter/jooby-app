$(document).ready(function () {
    $("[name='phone']").mask("0000000000");

    $("[name='streetName']").typeahead({
        source: function (query, process) {
            return $.ajax({
                type: 'POST',
                url: 'https://dadata.ru/api/v1/suggest/address',
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Token bf69a05b6ce842dcd0cbc159648d19a8c49fdf33"
                },
                data: JSON.stringify({"query": "Ярославль " + query}),
                success: function (result) {
                    var suggestions = new Set();
                    for (var s of result.suggestions) {
                        suggestions.add(s.data.street_with_type);
                    }
                    return process(Array.from(suggestions));
                }
            });
        }
    });

    $("#" + $("[name='errorField']").val()).addClass("has-error");

    $(".description" ).each(function( index ) {
        $(this).html($(this).text());
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
