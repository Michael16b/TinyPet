let petition = { title: "", content: "", tags: "" };

export default {
    view: function() {
        return m("form", {
            onsubmit: function(e) {
                e.preventDefault();
                m.request({
                    method: "POST",
                    url: "/api/petitions",
                    body: petition
                }).then(() => m.route.set("/"));
            }
        }, [
            m("input[placeholder=Title]", {
                oninput: e => petition.title = e.target.value,
                value: petition.title
            }),
            m("textarea[placeholder=Content]", {
                oninput: e => petition.content = e.target.value,
                value: petition.content
            }),
            m("input[placeholder=Tags (comma-separated)]", {
                oninput: e => petition.tags = e.target.value,
                value: petition.tags
            }),
            m("button[type=submit]", "Submit")
        ]);
    }
};
