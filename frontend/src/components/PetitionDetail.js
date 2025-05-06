export default {
    petition: {},
    oninit: function(vnode) {
        m.request({
            method: "GET",
            url: `/api/petitions/${vnode.attrs.id}`
        }).then(data => this.petition = data);
    },
    view: function(vnode) {
        return m("div", [
            m("h2", this.petition.title),
            m("p", this.petition.content),
            m("p", "Tags: " + this.petition.tags.join(", ")),
            m("button", {
                onclick: () => {
                    m.request({
                        method: "POST",
                        url: `/api/petitions/${vnode.attrs.id}/sign`
                    });
                }
            }, "Sign this Petition"),
            m("a", { href: `/signers/${vnode.attrs.id}`, oncreate: m.route.link }, "View Signers")
        ]);
    }
};
