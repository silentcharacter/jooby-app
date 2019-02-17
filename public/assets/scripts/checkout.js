$(document).ready(function () {
    $('select').niceSelect();
    //redraw time select options depending on date select value
    $($('div .nice-select')[0]).find('li').each(function( index ) {
        $(this).click(function () {
            var s = $('#datesSelect option[value="' + $(this).text().trim() + '"]').attr('data');
            $("#timeSelect option").remove();
            var array = s.split(',');
            for (var i = 0; i < array.length; i++) {
                $("#timeSelect").append('<option value="' + array[i] +'">' + array[i] + '</option>');
            }
            $('.nice-select')[1].remove();
            $('select').niceSelect();
        });
    });

    var validation = function(cep) {
        var masks = ['+7 (000) 000-00-00', '8 (000) 000-00-00'];
        var mask = masks[0];
        if (cep.startsWith('+7 (8')) {
            mask = masks[1];
            $("[name='phone']").val(cep.substring(4).replace(/\D/g,''));
        }
        if (cep.startsWith('8 (')) {
            mask = masks[1];
        }
        return mask;
    };

    var options =  {
        onChange: function(cep){
            $("[name='phone']").mask(validation(cep), options);
        },
        onKeyPress: function(cep, e, field, options) {
            $("[name='phone']").mask(validation(cep), options);
        }
    };

    $("[name='phone']").mask("+7 (000) 000-00-000", options);

    $("[name='entrance']").mask("0000");

    $("#streetName").typeahead({
        source: function (query, process) {
            return $.ajax({
                type: 'POST',
                url: 'https://suggestions.dadata.ru/suggestions/api/4_1/rs/suggest/address',
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json",
                    "Authorization": "Token 5f9a9639b143b728904625104e17412857145b77"
                },
                data: JSON.stringify({"query": "Ярославль " + query}),
                success: function (result) {
                    if (!result || !result.suggestions)
                        return [];
                    var suggestions = new Set();
                    for (var i = 0; i < result.suggestions.length; i++) {
                        suggestions.add(result.suggestions[i].data.street_with_type);
                    }
                    return process(Array.from(suggestions));
                }
            });
        }
    });

    $("#" + $("[name='errorField']").val()).addClass("has-error");

});

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
