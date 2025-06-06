import {Component, OnInit} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import {UserService} from '../services/user.service';
import {NgIf} from '@angular/common';
import {LoginDialogComponent} from '../dialogs/login-dialog/login-dialog.component';
import { ChangeDetectorRef } from '@angular/core';


@Component({
  selector: 'app-banner',
  templateUrl: './banner.component.html',
  imports: [
    NgIf
  ],
  styleUrls: ['./banner.component.css']
})
export class BannerComponent implements OnInit {
  isLoggedIn = false;
  username: string | null = null;
  picture: string | null = null;
  private lastKnownToken: string | null = null;
  private tokenCheckStarted = false;

  constructor(
    private dialog: MatDialog,
    private userService: UserService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.refreshUser();
    this.userService.setLoginCallback(() => {
      this.refreshUser();
      this.cdr.detectChanges();
    });
  }

  openLoginDialog(): void {
    const dialogRef = this.dialog.open(LoginDialogComponent, {
      width: '370px',
    });
    dialogRef.afterClosed().subscribe(() => {
      this.refreshUser();
    });
  }

  logout(): void {
    this.userService.logout();

    this.isLoggedIn = false;
    this.username = null;
    this.picture = null;
    this.tokenCheckStarted = false;
    this.cdr.detectChanges();
  }


  refreshUser(): void {
    this.isLoggedIn = this.userService.isUserLoggedIn();
    this.username = localStorage.getItem('name');
    this.picture = localStorage.getItem('picture');

    // Lancer la surveillance du token seulement si connecté
    if (this.isLoggedIn && !this.tokenCheckStarted) {
      this.tokenCheckStarted = true;
      this.lastKnownToken = this.userService.getAccessToken();

      setInterval(() => {
        const currentToken = this.userService.getAccessToken();

        if (this.lastKnownToken !== currentToken) {
          console.log('⚠️ Access token modifié, on déconnecte');
          this.logout();
          this.cdr.detectChanges();
        }

        this.lastKnownToken = currentToken;
      }, 5_000);
    }
  }
}
