import {Component, OnInit} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { LoginComponent } from '../login/login.component';
import {UserService} from '../services/user.service';
import {NgIf} from '@angular/common';
import {LoginDialogComponent} from '../dialogs/login-dialog/login-dialog.component'; // adapte le chemin si besoin

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

  constructor(
    private dialog: MatDialog,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.refreshUser();
    // Si tu utilises un EventEmitter pour login/logout, abonne-toi ici pour refresh dynamiquement
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
    this.refreshUser();
  }

  refreshUser(): void {
    this.isLoggedIn = this.userService.isUserLoggedIn();
    this.username = localStorage.getItem('name');
    this.picture = localStorage.getItem('picture');
  }
}
