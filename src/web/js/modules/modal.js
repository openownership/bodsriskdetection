$(document).ready(function ($) {

    $(document).on("click", ".modal", function (event) {
        $(event.target).remove();
    });

    $(document).on("click", ".modal-dialog", function (event) {
        event.stopPropagation();
    });

    $(document).on("click", ".modal-close", function (event) {
        $(event.target).closest(".modal").remove();
    });

    function ajaxModal(title, url) {
        showModal(title);
        ajaxGet(url, function (response) {
            $(".modal-content").html(response);
        })
    }

    $("*[data-ajax-modal-url]").click(function (event) {
        let url = $(event.target).attr("data-ajax-modal-url");
        let title = $(event.target).attr("data-ajax-modal-title");
        showModal(title);
        ajaxGet(url, function (response) {
            $(".modal-content").html(response);
        })
    });
});

exports.showModal = function (title) {
    let html = `
            <div class="modal">
                <div class="modal-dialog">
                    <div class="modal-title flex flex-row justify-between items-center">
                        <div>${title}</div>
                        <a href="#" class="modal-close rounded-lg bg-slate-100 p-2 group">
                            <svg version="1.1" viewBox="0 0 1200 1200" xmlns="http://www.w3.org/2000/svg" class="fill-slate-400 group-hover:fill-control w-4 h-4">
                                <path d="m264 936c12 12 27.602 18 42 18s31.199-6 42-18l252-252 252 252c12 12 27.602 18 42 18s31.199-6 42-18c24-24 24-61.199 0-85.199l-252-252 252-252c24-24 24-61.199 0-85.199s-61.199-24-85.199 0l-252 252-250.8-249.6c-24-24-61.199-24-85.199 0s-24 61.199 0 85.199l252 252-250.8 250.8c-24 22.801-24 61.199 0 84z"/>
                            </svg>
                        </a>
                    </div>
                    <div class="modal-content">
                    </div>
                </div>
            </div>`;
    $("body").append(html);
}
