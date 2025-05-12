import { Injectable } from '@angular/core';
import { CookiesService } from './cookies.service';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  constructor(private readonly cookiesService: CookiesService) {}

  login(id: string, name: string, family_name: string, picture: string, email: string): void {
    this.cookiesService.setCookie('id_user', id);
    this.cookiesService.setCookie('name', name);
    this.cookiesService.setCookie('family_name', family_name);
    this.cookiesService.setCookie('picture', picture);
    this.cookiesService.setCookie('email', email);

  }

  logout(): void {
    this.cookiesService.deleteCookie('id_user');
    this.cookiesService.deleteCookie('name');
    this.cookiesService.deleteCookie('family_name');
    this.cookiesService.deleteCookie('picture');
    this.cookiesService.deleteCookie('email');
  }

  isUserLoggedIn(): boolean {
    const idUser = this.cookiesService.getCookie('id_user');
    const username = this.cookiesService.getCookie('username');
    const email = this.cookiesService.getCookie('email');

    return !!(idUser && username && email);
  }
}
