$(document).ready(function () {
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
});
