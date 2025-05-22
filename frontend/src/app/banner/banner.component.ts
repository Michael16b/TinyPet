import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { LoginComponent } from '../login/login.component'; // adapte le chemin si besoin

@Component({
  selector: 'app-banner',
  templateUrl: './banner.component.html',
  styleUrls: ['./banner.component.css']
})
export class BannerComponent {
  constructor(private dialog: MatDialog) {}

  openLoginDialog(): void {
    this.dialog.open(LoginComponent, {
      width: '300px',
    });
  }
}
