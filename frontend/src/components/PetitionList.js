export default {
    petitions: [],
    oninit: function() {
        m.request({
            method: "GET",
            url: "/api/petitions/top100" // REST endpoint
        }).then(result => this.petitions = result);
    },
    view: function() {
        return m("div", [
            m("h2", "Top 100 Petitions"),
            m("ul",
                this.petitions.map(p =>
                    m("li", [
                        m("a", { href: "/petition/" + p.id, oncreate: m.route.link }, p.title)
                    ])
                )
            ),
            m("a", { href: "/create", oncreate: m.route.link }, "Create Petition")
        ]);
    }
};
