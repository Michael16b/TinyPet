import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {BannerComponent} from './banner/banner.component';
import {NgOptimizedImage} from '@angular/common';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, BannerComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'tinyPet-app';
}
