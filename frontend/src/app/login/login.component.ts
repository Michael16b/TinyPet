import {AfterViewInit, Component, OnInit} from '@angular/core';
import {jwtDecode} from 'jwt-decode';
import {GoogleJwtPayload} from '../interfaces/google-jwt-payload';
import {CookiesService} from '../services/cookies.service';
import {UserService} from '../services/user.service';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements AfterViewInit {

  constructor(
    private readonly userService: UserService,
  ) {
    }
  ngAfterViewInit(): void {
    const google = (window as any).google;
    if (google && google.accounts) {
      google.accounts.id.initialize({
        client_id: '598050199229-8svis83vs9bug6d5tpjqjta3jnbusdan.apps.googleusercontent.com',
        callback: this.handleCredentialResponse.bind(this),
      });
      google.accounts.id.renderButton(
        document.getElementById('googleSignInButton') as HTMLElement,
        {theme: 'outline', size: 'large'}
      );
    } else {
      console.error('Google Identity Services script not loaded.');
    }
  }

  handleCredentialResponse(response: any): void {
    console.log('ID Token:', response.credential);
    const responsePayload = jwtDecode<GoogleJwtPayload>(response.credential);
    // console.log("ID: " + responsePayload.id);
    // console.log('Full Name: ' + responsePayload.name);
    // console.log('Given Name: ' + responsePayload.given_name);
    // console.log('Family Name: ' + responsePayload.family_name);
    // console.log("Image URL: " + responsePayload.picture);
    // console.log("Email: " + responsePayload.email);

    this.userService.login(
      responsePayload.id,
      response.credential,
      responsePayload.name,
      responsePayload.family_name,
      responsePayload.picture,
      responsePayload.email);
  }

}
