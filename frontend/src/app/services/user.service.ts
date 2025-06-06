import { Injectable } from '@angular/core';
import { CookiesService } from './cookies.service';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  constructor() {}

  login(id: string, access_token: string, name: string, family_name: string, picture: string, email: string): void {
    localStorage.setItem('id_user', id);
    localStorage.setItem('access_token', access_token);
    localStorage.setItem('name', name);
    localStorage.setItem('family_name', family_name);
    localStorage.setItem('picture', picture);
    localStorage.setItem('email', email);
  }

  logout(): void {
    localStorage.removeItem('id_user');
    localStorage.removeItem('access_token');
    localStorage.removeItem('name');
    localStorage.removeItem('family_name');
    localStorage.removeItem('picture');
    localStorage.removeItem('email');
  }

  getAccessToken(): string | null {
    return localStorage.getItem('access_token');
  }




  isUserLoggedIn(): boolean {
    const idUser = localStorage.getItem('id_user');
    const username = localStorage.getItem('name');
    const email = localStorage.getItem('email');

    return !!(idUser && username && email);
  }

}
