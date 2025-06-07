import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { UserService } from '../services/user.service';
import { NgIf } from '@angular/common';
import { LoginDialogComponent } from '../dialogs/login-dialog/login-dialog.component';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-banner',
  templateUrl: './banner.component.html',
  imports: [NgIf],
  styleUrls: ['./banner.component.css']
})
export class BannerComponent implements OnInit {
  isLoggedIn: boolean = false;
  picture: string | null = '';
  familyName: string = '';
  name: string = '';

  constructor(
    private dialog: MatDialog,
    private userService: UserService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.updateUser();
    this.userService.refreshUser();

    // Mettre à jour l'affichage toutes les 3 secondes
    setInterval(() => {
      this.updateUser();
      this.cdr.detectChanges();
    }, 3000);

    // Callback sur login ou logout
    this.userService.setLoginCallback(() => {
      this.updateUser();
      this.cdr.detectChanges();
    });
  }

  openLoginDialog(): void {
    const dialogRef = this.dialog.open(LoginDialogComponent, {
      width: '370px',
    });

    dialogRef.afterClosed().subscribe(() => {
      this.updateUser();
    });
  }

  logout(): void {
    this.userService.logout();
    this.updateUser();
    this.cdr.detectChanges();
  }

  updateUser(): void {
    this.isLoggedIn = this.userService.getIsLoggedIn();
    this.picture = this.userService.getPicture();
    this.familyName = this.userService.getFamilyName();
    this.name = this.userService.getName();
  }
}
