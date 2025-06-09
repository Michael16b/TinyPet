import { Routes } from '@angular/router';
import {LoginComponent} from './login/login.component';
import {HomeComponent} from './home/home.component';
import {PetitionComponent} from './petition/petition.component';
import {PetitionDetailsComponent} from './petition/petition-details/petition-details.component';
import {TopPetitionComponent} from './petition/top-petition/top-petition.component';

export const routes: Routes = [
  {path: '', redirectTo: '/home', pathMatch: 'full'},
  {path: 'home', component: HomeComponent},
  {path: 'login', component: LoginComponent},
  {path: 'petition', component: PetitionComponent},
  { path: 'petition/:id', component: PetitionDetailsComponent },
  {path: 'top100petitions', component: TopPetitionComponent},
];
