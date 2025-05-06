import PetitionList from "./components/PetitionList.js";
import PetitionDetail from "./components/PetitionDetail.js";
import PetitionForm from "./components/PetitionForm.js";
import SignersList from "./components/SignerList.js";

m.route(document.body, "/", {
    "/": PetitionList,
    "/petition/:id": PetitionDetail,
    "/create": PetitionForm,
    "/signers/:id": SignersList
});
