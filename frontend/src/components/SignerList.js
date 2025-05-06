export default {
    signers: [],
    oninit: function(vnode) {
        const petitionId = vnode.attrs.id;

        m.request({
            method: "GET",
            url: `/api/petitions/${petitionId}/signers`
        }).then(data => {
            this.signers = data;
        });
    },
    view: function(vnode) {
        return m("div", [
            m("h2", "Signers of Petition " + vnode.attrs.id),
            this.signers.length === 0
                ? m("p", "No signers yet.")
                : m("ul",
                    this.signers.map(signer =>
                        m("li", signer.name || signer.email || "Anonymous")
                    )
                ),
            m("a", { href: "/", oncreate: m.route.link }, "← Back to list")
        ]);
    }
};
