import { AfterViewInit, Component } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from '@angular/material/dialog';
import { UserService } from '../../services/user.service';
import {jwtDecode} from 'jwt-decode';
import {MatButton} from '@angular/material/button';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatError, MatFormField, MatLabel} from '@angular/material/input';
import {NgIf} from '@angular/common';

interface GoogleJwtPayload {
  id: string;
  name: string;
  family_name: string;
  picture: string;
  email: string;
}

@Component({
  selector: 'app-login-dialog',
  templateUrl: './login-dialog.component.html',
  styleUrls: ['./login-dialog.component.css'],
  imports: [
    ReactiveFormsModule,
    MatButton,
    MatProgressSpinner,
    MatDialogContent,
    MatDialogTitle,
    NgIf,
    MatDialogActions,
    MatDialogClose,
  ]
})
export class LoginDialogComponent implements AfterViewInit {
  loginForm: FormGroup;
  loading = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private dialogRef: MatDialogRef<LoginDialogComponent>
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
    });
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
        { theme: 'outline', size: 'large' }
      );
    } else {
      console.error('Google Identity Services script not loaded.');
    }
  }

  handleCredentialResponse(response: any): void {
    const responsePayload = jwtDecode<GoogleJwtPayload>(response.credential);
    this.userService.login(
      responsePayload.id,
      response.credential,
      responsePayload.name,
      responsePayload.family_name,
      responsePayload.picture,
      responsePayload.email
    );
    this.dialogRef.close(true);
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }
    this.loading = true;
    this.error = null;
  }
}
