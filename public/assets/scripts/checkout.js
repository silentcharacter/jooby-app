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

    $("[name='phone']").mask("+7 (000) 000-00-00", {
         translation: {
           'r': {
             pattern: /[\/]/,
           },
         }
    });

    $("[name='entrance']").mask("0000");

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
