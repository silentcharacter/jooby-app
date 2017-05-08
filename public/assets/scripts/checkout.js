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

    $('#phone').focusout(function() {
        if ($(this).val() && !$('#name').val()) {
            $.ajax({
                type: 'GET',
                url: '/orderByPhone?phone=' + $(this).val(),
                success: function (result) {
                    $('#name').val(result.name);
                    $('#streetName').val(result.streetName);
                    $('#streetNumber').val(result.streetNumber);
                    $('#flat').val(result.flat);
                    $('#entrance').val(result.entrance);
                }
            });
        }
    });

    $("[name='phone']").mask("0000000000");

    $("#streetName").typeahead({
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
                    for (var s in result.suggestions) {
                        suggestions.add(s.data.street_with_type);
                    }
                    return process(Array.from(suggestions));
                }
            });
        }
    });

    $("#" + $("[name='errorField']").val()).addClass("has-error");

});
